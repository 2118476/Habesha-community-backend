package com.habesha.community.controller;

import com.habesha.community.dto.ServiceDetailDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.repository.ServiceOfferRepository;
import com.habesha.community.repository.ServiceReviewRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API controller for service marketplace endpoints.  Provides
 * pagination, filtering and detail retrieval adhering to the new
 * specification.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ApiServiceController {

    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final UserService userService;
    private final com.habesha.community.service.SupabaseStorageService supabaseStorage;

    /**
     * List available services with optional search and price filters.
     */
    @GetMapping
    public ResponseEntity<List<ServiceDetailDto>> listServices(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,DESC") String sort
    ) {
        Sort.Direction dir = Sort.Direction.DESC;
        String sortProp = "createdAt";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortProp = parts[0];
            dir = parts[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortProp));
        // For now we ignore search and category filters; they could be implemented via Specifications
        Page<ServiceOffer> pageData = serviceOfferRepository.findAll(pageable);
        List<ServiceDetailDto> dtos = pageData.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get details for a single service offer.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDetailDto> getService(@PathVariable Long id) {
        ServiceOffer offer = serviceOfferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found"));
        return ResponseEntity.ok(toDto(offer));
    }

    private ServiceDetailDto toDto(ServiceOffer offer) {
        UserSummaryDto author = userService.toSummary(offer.getProvider());

        // Review aggregates (so cards can show ★ rating + count without an extra call)
        Double rating = null;
        Long reviewCount = null;
        if (offer.getProvider() != null && offer.getProvider().getId() != null) {
            Long providerId = offer.getProvider().getId();
            reviewCount = serviceReviewRepository.countByProvider_Id(providerId);
            Double avg = serviceReviewRepository.averageRating(providerId);
            if (avg != null) {
                rating = Math.round(avg * 10.0) / 10.0; // one decimal place
            }
        }

        return ServiceDetailDto.builder()
                .id(offer.getId())
                .category(offer.getCategory())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .estimatedTime(offer.getEstimatedTime())
                .price(offer.getBasePrice())
                .rateUnit("fixed")
                .location(offer.getLocation())
                .mode(offer.getMode() != null ? offer.getMode().name() : null)
                .tags(Collections.emptyList())
                .featured(offer.isFeatured())
                .createdAt(offer.getCreatedAt())
                .imageUrl(offer.getImageUrl())
                .rating(rating)
                .reviewCount(reviewCount)
                .postedBy(author)
                .author(author)
                .build();
    }

    /** Stream the service's cover image (public). */
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getServiceImage(@PathVariable Long id) {
        ServiceOffer offer = serviceOfferRepository.findById(id).orElse(null);
        if (offer == null || offer.getImageData() == null || offer.getImageData().length == 0) {
            return ResponseEntity.notFound().build();
        }
        String mime = offer.getImageContentType() != null ? offer.getImageContentType() : "image/jpeg";
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(mime))
                .cacheControl(org.springframework.http.CacheControl
                        .maxAge(java.time.Duration.ofDays(30)).cachePublic())
                .body(offer.getImageData());
    }

    /** Alias used by the feed image resolver. */
    @GetMapping("/{id}/photos/first")
    public ResponseEntity<byte[]> getServiceFirstPhoto(@PathVariable Long id) {
        return getServiceImage(id);
    }

    /** Upload / replace the cover image for a service (owner only). */
    @PostMapping("/{id}/image")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ServiceDetailDto> uploadServiceImage(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        var me = userService.getCurrentUser().orElseThrow(() ->
                new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        ServiceOffer offer = serviceOfferRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Service not found"));
        if (offer.getProvider() != null && !offer.getProvider().getId().equals(me.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "No file uploaded");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        try {
            if (supabaseStorage.isEnabled()) {
                // Preferred path (same as rentals/avatars): store bytes on Supabase
                // Storage and keep only the public URL — no Postgres blob.
                String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "cover.jpg";
                String publicUrl = supabaseStorage.upload("service/" + id, original, file.getBytes(), contentType);
                // Replace any previous image (best-effort cleanup of the old object).
                if (offer.getImagePath() != null) {
                    supabaseStorage.deleteByPublicUrl(offer.getImagePath());
                }
                offer.setImagePath(publicUrl);
                offer.setImageData(null);
                offer.setImageContentType(contentType);
            } else {
                // Legacy fallback when Supabase is not configured: DB blob.
                offer.setImageData(file.getBytes());
                offer.setImageContentType(contentType);
            }
        } catch (java.io.IOException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Could not read uploaded file");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed: " + e.getMessage());
        }
        serviceOfferRepository.save(offer);
        return ResponseEntity.ok(toDto(offer));
    }


    /**
     * Create a new ServiceOffer for the current user.
     */
    @PostMapping
    public ResponseEntity<ServiceOffer> createServiceOffer(@RequestBody ServiceOffer body) {
        var me = userService.getCurrentUser().orElseThrow(() -> 
            new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        body.setProvider(me);
        var saved = serviceOfferRepository.save(body);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing ServiceOffer owned by the current user.
     */
    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ServiceOffer> updateServiceOffer(@PathVariable Long id, @RequestBody ServiceOffer body) {
        var me = userService.getCurrentUser().orElseThrow(() ->
            new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        var existing = serviceOfferRepository.findById(id).orElseThrow(() ->
            new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        // Only the provider can update
        if (existing.getProvider() != null && !existing.getProvider().getId().equals(me.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }

        // Patch only the editable fields that were provided. Everything else —
        // notably the cover image (imagePath/imageData), provider and createdAt —
        // is left untouched, so editing text never wipes the media or the price.
        if (body.getCategory() != null) existing.setCategory(body.getCategory());
        if (body.getTitle() != null) existing.setTitle(body.getTitle());
        if (body.getDescription() != null) existing.setDescription(body.getDescription());
        if (body.getEstimatedTime() != null) existing.setEstimatedTime(body.getEstimatedTime());
        if (body.getBasePrice() != null) existing.setBasePrice(body.getBasePrice());
        if (body.getLocation() != null) existing.setLocation(body.getLocation());
        if (body.getMode() != null) existing.setMode(body.getMode());

        var saved = serviceOfferRepository.save(existing);
        return ResponseEntity.ok(saved);
    }


}
