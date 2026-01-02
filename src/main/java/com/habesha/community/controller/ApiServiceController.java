package com.habesha.community.controller;

import com.habesha.community.dto.ServiceDetailDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.repository.ServiceOfferRepository;
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
    private final UserService userService;

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
        return ServiceDetailDto.builder()
                .id(offer.getId())
                .category(offer.getCategory())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .estimatedTime(offer.getEstimatedTime())
                .price(offer.getBasePrice())
                .rateUnit("fixed")
                .location(offer.getLocation())
                .tags(Collections.emptyList())
                .featured(offer.isFeatured())
                .createdAt(offer.getCreatedAt())
                .postedBy(author)
                .author(author)
                .build();
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
public ResponseEntity<ServiceOffer> updateServiceOffer(@PathVariable Long id, @RequestBody ServiceOffer body) {
    var me = userService.getCurrentUser().orElseThrow(() ->
        new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
    var existing = serviceOfferRepository.findById(id).orElseThrow(() ->
        new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

    // Only the provider can update
    if (existing.getProvider() != null && !existing.getProvider().getId().equals(me.getId())) {
        throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    body.setId(existing.getId());
    // Preserve original provider if present, otherwise set to current user
    if (existing.getProvider() != null) {
        body.setProvider(existing.getProvider());
    } else {
        body.setProvider(me);
    }

    var saved = serviceOfferRepository.save(body);
    return ResponseEntity.ok(saved);
}


}
