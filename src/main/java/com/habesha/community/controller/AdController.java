package com.habesha.community.controller;

import com.habesha.community.dto.AdDetailsDto;
import com.habesha.community.dto.ClassifiedAdRequest;
import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.AdPhoto;
import com.habesha.community.model.User;
import com.habesha.community.repository.AdPhotoRepository;
import com.habesha.community.service.AdService;
import com.habesha.community.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.nio.file.Path;

/**
 * REST endpoints for classified advertisements.
 *
 * Notes:
 * - Exposed under BOTH "/ads" and "/api/ads" so frontend can call /api/ads.
 * - GET /api/ads?page=0&size=36 is used by the global search.
 * - List endpoint does safe, in-memory pagination on top of AdService.listAds(...).
 */
@RestController
@RequestMapping({"/ads", "/api/ads"})
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;
    private final AdPhotoRepository adPhotoRepository;
    private final FileStorageService storage;

    /* -------------------------------------------------------------------------
     * CREATE
     * ---------------------------------------------------------------------- */

    @PostMapping
    public ResponseEntity<ClassifiedAd> createAd(
            @Valid @RequestBody ClassifiedAdRequest request
    ) {
        ClassifiedAd created = adService.createAd(request);
        
        // TEMPORARY TEST: Set a hardcoded imageUrl to test display
        // created.setImageUrl("/uploads/ads/test/sample.jpg");
        // adService.saveAd(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* -------------------------------------------------------------------------
     * LIST (with safe pagination)
     *
     * Used by:
     * - Ads list pages in the app.
     * - Global search (calls /api/ads?page=0&size=36).
     * ---------------------------------------------------------------------- */

    @GetMapping
    public ResponseEntity<List<ClassifiedAd>> listAds(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "36") int size
    ) {
        // Pull all ads (optionally filtered by category)
        List<ClassifiedAd> all = adService.listAds(Optional.ofNullable(category));

        // Defensive pagination on top of the list result
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100); // cap to avoid huge pages
        int fromIndex = safePage * safeSize;

        if (fromIndex >= all.size()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        int toIndex = Math.min(fromIndex + safeSize, all.size());
        List<ClassifiedAd> pageSlice = all.subList(fromIndex, toIndex);

        return ResponseEntity.ok(pageSlice);
    }

    /* -------------------------------------------------------------------------
     * READ SINGLE (details view)
     *
     * Returns rich DTO with likeCount, likedByMe, poster info, etc.
     * ---------------------------------------------------------------------- */

    @GetMapping("/{id}")
    public ResponseEntity<AdDetailsDto> getAd(@PathVariable Long id) {
        AdDetailsDto dto = adService.getAdDetails(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Lightweight HEAD endpoint so the frontend can quickly check if
     * an ad still exists (used by search "filterExistingItems").
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headAd(@PathVariable Long id) {
        try {
            // If this throws, we treat as not found
            adService.getAdDetails(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /* -------------------------------------------------------------------------
     * UPDATE
     * ---------------------------------------------------------------------- */

    @PutMapping("/{id}")
    public ResponseEntity<ClassifiedAd> updateAd(
            @PathVariable Long id,
            @Valid @RequestBody ClassifiedAdRequest request
    ) {
        ClassifiedAd updated = adService.updateAd(id, request);
        return ResponseEntity.ok(updated);
    }

    /* -------------------------------------------------------------------------
     * DELETE
     * ---------------------------------------------------------------------- */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }

    /* -------------------------------------------------------------------------
     * LIKE / UNLIKE
     * ---------------------------------------------------------------------- */

    @PostMapping("/{id}/like")
    public ResponseEntity<Long> likeAd(@PathVariable Long id) {
        long newCount = adService.likeAd(id);
        return ResponseEntity.ok(newCount);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Long> unlikeAd(@PathVariable Long id) {
        long newCount = adService.unlikeAd(id);
        return ResponseEntity.ok(newCount);
    }

    /* -------------------------------------------------------------------------
     * DEBUG ENDPOINT - Remove after testing
     * ---------------------------------------------------------------------- */
    @GetMapping("/debug/photos")
    public ResponseEntity<Map<String, Object>> debugPhotos() {
        try {
            // Try to count all AdPhoto entities
            long count = adPhotoRepository.count();
            
            // Try to find all AdPhoto entities
            List<AdPhoto> allPhotos = adPhotoRepository.findAll();
            
            // Try to get an ad to test the relationship
            ClassifiedAd testAd = adService.getAdByIdWithPhotos(1L);
            
            return ResponseEntity.ok(Map.of(
                "totalPhotos", count,
                "allPhotos", allPhotos.stream().map(p -> Map.of(
                    "id", p.getId(),
                    "filename", p.getFilename(),
                    "adId", p.getAd() != null ? p.getAd().getId() : null,
                    "sortIndex", p.getSortIndex()
                )).collect(Collectors.toList()),
                "testAd", testAd != null ? Map.of(
                    "id", testAd.getId(),
                    "title", testAd.getTitle(),
                    "photosCount", testAd.getPhotos() != null ? testAd.getPhotos().size() : "null"
                ) : "not found"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "stackTrace", java.util.Arrays.toString(e.getStackTrace())
            ));
        }
    }

    @PostMapping("/debug/test-photo")
    @Transactional
    public ResponseEntity<Map<String, Object>> testPhotoCreation() {
        try {
            // Get ad 1
            ClassifiedAd ad = adService.getAdByIdWithPhotos(1L);
            if (ad == null) {
                return ResponseEntity.ok(Map.of("error", "Ad 1 not found"));
            }

            // Create a test photo
            AdPhoto testPhoto = AdPhoto.builder()
                    .filename("test.jpg")
                    .filePath("/test/path/test.jpg")
                    .sortIndex(0)
                    .ad(ad)
                    .build();

            AdPhoto saved = adPhotoRepository.save(testPhoto);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "photoId", saved.getId(),
                "filename", saved.getFilename(),
                "adId", saved.getAd().getId()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "stackTrace", java.util.Arrays.toString(e.getStackTrace())
            ));
        }
    }

    /* -------------------------------------------------------------------------
     * PHOTO UPLOAD
     * ---------------------------------------------------------------------- */

    @PostMapping("/{id}/photos")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {
        
        System.out.println("=== PHOTO UPLOAD DEBUG ===");
        System.out.println("Ad ID: " + id);
        System.out.println("Files received: " + files.size());
        
        ClassifiedAd ad = adService.getAdByIdWithPhotos(id);
        if (ad == null) {
            System.out.println("Ad not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }

        System.out.println("Ad found: " + ad.getTitle());
        System.out.println("Existing photos: " + (ad.getPhotos() != null ? ad.getPhotos().size() : "null"));

        if (CollectionUtils.isEmpty(files)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No files provided"));
        }

        Path base = Path.of("uploads", "ads", String.valueOf(id)).toAbsolutePath();
        System.out.println("Upload directory: " + base);
        
        int nextIndex = (ad.getPhotos() == null) ? 0 : ad.getPhotos().size();
        System.out.println("Next sort index: " + nextIndex);

        List<Map<String, Object>> saved = new ArrayList<>();

        for (MultipartFile mf : files) {
            if (mf.isEmpty()) continue;

            String original = Objects.requireNonNullElse(mf.getOriginalFilename(), "image.jpg");
            String safe = storage.safeFilename(original);
            Path savedPath = storage.saveStream(base, safe, mf.getInputStream());

            System.out.println("File saved to: " + savedPath);

            AdPhoto photo = AdPhoto.builder()
                    .filename(safe)
                    .filePath(savedPath.toString())
                    .sortIndex(nextIndex++)
                    .ad(ad)
                    .build();

            System.out.println("Creating AdPhoto: filename=" + safe + ", sortIndex=" + photo.getSortIndex() + ", adId=" + ad.getId());

            try {
                AdPhoto savedPhoto = adPhotoRepository.save(photo);
                System.out.println("Successfully saved AdPhoto with ID: " + savedPhoto.getId());

                saved.add(Map.of(
                        "id", savedPhoto.getId(),
                        "filename", safe,
                        "url", "/ads/photos/" + savedPhoto.getId(),
                        "size", mf.getSize()
                ));
            } catch (Exception e) {
                System.err.println("Error saving AdPhoto: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        System.out.println("Total photos saved: " + saved.size() + " for ad: " + id);
        System.out.println("=== END PHOTO UPLOAD DEBUG ===");
        
        return ResponseEntity.ok(Map.of(
                "message", "Photos uploaded successfully",
                "count", saved.size(),
                "photos", saved
        ));
    }

    /* -------------------------------------------------------------------------
     * READ (ONE) with lightweight photos info
     * GET /ads/{id}/with-photos
     * ---------------------------------------------------------------------- */
    @GetMapping("/{id}/with-photos")
    public ResponseEntity<Map<String, Object>> getAdWithPhotos(@PathVariable Long id) {
        ClassifiedAd ad = adService.getAdByIdWithPhotos(id);
        if (ad == null) {
            return ResponseEntity.notFound().build();
        }

        System.out.println("Loading ad " + id + " with photos. Photos count: " + 
                          (ad.getPhotos() != null ? ad.getPhotos().size() : "null"));

        List<Map<String, Object>> photos = (
                ad.getPhotos() == null
                        ? List.<AdPhoto>of()
                        : ad.getPhotos()
        )
                .stream()
                .sorted(
                        Comparator
                                .comparing(AdPhoto::getSortIndex)
                                .thenComparing(AdPhoto::getId)
                )
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "url", "/ads/photos/" + p.getId(),
                        "filename", p.getFilename(),
                        "sortIndex", p.getSortIndex()
                ))
                .collect(Collectors.toList());

        // Get poster information
        User poster = ad.getPoster();
        Long posterId = poster != null ? poster.getId() : null;
        String posterName = "Community member";
        if (poster != null) {
            if (poster.getName() != null && !poster.getName().isBlank()) {
                posterName = poster.getName();
            } else if (poster.getEmail() != null && !poster.getEmail().isBlank()) {
                posterName = poster.getEmail();
            }
        }
        String posterAvatar = (poster != null) ? poster.getAvatarUrl() : null;

        // Get like information using the same logic as AdDetailsDto
        AdDetailsDto adDetails = adService.getAdDetails(id);
        long likeCount = adDetails != null ? adDetails.getLikeCount() : 0;
        boolean likedByMe = adDetails != null ? adDetails.isLikedByMe() : false;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", ad.getId());
        payload.put("title", ad.getTitle());
        payload.put("description", ad.getDescription());
        payload.put("price", ad.getPrice());
        payload.put("category", ad.getCategory());
        payload.put("featured", ad.isFeatured());
        payload.put("createdAt", ad.getCreatedAt());
        payload.put("photos", photos);
        payload.put("photosCount", photos.size());
        payload.put("firstPhotoUrl", photos.isEmpty() ? null : photos.get(0).get("url"));
        
        // Add poster information
        payload.put("posterId", posterId);
        payload.put("posterName", posterName);
        payload.put("posterAvatar", posterAvatar);
        
        // Add like information
        payload.put("likeCount", likeCount);
        payload.put("likedByMe", likedByMe);

        System.out.println("Returning ad with " + photos.size() + " photos, poster: " + posterName + 
                          ", likes: " + likeCount + ", likedByMe: " + likedByMe);
        return ResponseEntity.ok(payload);
    }
}
