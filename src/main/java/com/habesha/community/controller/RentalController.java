package com.habesha.community.controller;

import com.habesha.community.dto.RentalRequest;
import com.habesha.community.dto.RentalUpdateRequest;
import com.habesha.community.model.Rental;
import com.habesha.community.model.RentalPhoto;
import com.habesha.community.repository.RentalPhotoRepository;
import com.habesha.community.repository.RentalRepository;
import com.habesha.community.service.FileStorageService;
import com.habesha.community.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final RentalRepository rentalRepository;
    private final RentalPhotoRepository rentalPhotoRepository;
    private final FileStorageService storage;

    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<Rental> createRental(
            @Valid @RequestBody RentalRequest request
    ) {
        return ResponseEntity.ok(rentalService.createRental(request));
    }

    // -------------------------------------------------------------------------
    // READ (LIST)
    // Optional filter by ?city=
    // -------------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Rental>> listRentals(
            @RequestParam(name = "city", required = false) String city
    ) {
        return ResponseEntity.ok(
                rentalService.listRentals(Optional.ofNullable(city))
        );
    }

    // -------------------------------------------------------------------------
    // READ (ONE)
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRental(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.getRental(id));
    }

    // -------------------------------------------------------------------------
    // UPDATE (EDIT LISTING)
    // Owner or ADMIN only.
    // Body: { title, description, price, location }
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Rental> updateRental(
            @PathVariable Long id,
            @RequestBody RentalUpdateRequest request
    ) {
        return ResponseEntity.ok(rentalService.updateRental(id, request));
    }

    // -------------------------------------------------------------------------
    // DELETE
    // Owner or ADMIN only.
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // PHOTOS: upload multiple images for a rental
    // POST /rentals/{id}/photos   multipart/form-data
    // -------------------------------------------------------------------------
    @PostMapping("/{id}/photos")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadPhotos(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files
    ) throws Exception {

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        if (CollectionUtils.isEmpty(files)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No files provided"));
        }

        Path base = Path.of("uploads", "rental", String.valueOf(id)).toAbsolutePath();
        int nextIndex = (rental.getPhotos() == null) ? 0 : rental.getPhotos().size();

        List<Map<String, Object>> saved = new ArrayList<>();

        for (MultipartFile mf : files) {
            if (mf.isEmpty()) continue;

            String original = Objects.requireNonNullElse(mf.getOriginalFilename(), "image.jpg");
            String safe = storage.safeFilename(original);
            Path savedPath = storage.saveStream(base, safe, mf.getInputStream());

            RentalPhoto photo = RentalPhoto.builder()
                    .filename(safe)
                    .filePath(savedPath.toString())
                    .sortIndex(nextIndex++)
                    .rental(rental)
                    .build();

            rental.addPhoto(photo);
            rentalPhotoRepository.save(photo);

            saved.add(Map.of(
                    "id", photo.getId(),
                    "filename", photo.getFilename(),
                    "url", "/rentals/photos/" + photo.getId()
            ));
        }

        rentalRepository.save(rental);

        return ResponseEntity.ok(Map.of(
                "count", saved.size(),
                "photos", saved
        ));
    }

    // -------------------------------------------------------------------------
    // PHOTOS: delete a single photo
    // DELETE /rentals/{id}/photos/{photoId}
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}/photos/{photoId}")
    @Transactional
    public ResponseEntity<Void> deletePhoto(
            @PathVariable Long id,
            @PathVariable Long photoId
    ) {
        RentalPhoto p = rentalPhotoRepository.findById(photoId).orElse(null);
        if (p == null || !Objects.equals(p.getRental().getId(), id)) {
            return ResponseEntity.notFound().build();
        }

        p.getRental().removePhoto(p);
        rentalPhotoRepository.delete(p);

        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // READ (ONE) with lightweight photos info
    // GET /rentals/{id}/with-photos
    // -------------------------------------------------------------------------
    @GetMapping("/{id}/with-photos")
    public ResponseEntity<Map<String, Object>> getRentalWithPhotos(
            @PathVariable Long id
    ) {
        Rental r = rentalRepository.findById(id).orElse(null);
        if (r == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> photos = (
                r.getPhotos() == null
                        ? List.<RentalPhoto>of()
                        : r.getPhotos()
        )
                .stream()
                .sorted(
                        Comparator
                                .comparing(RentalPhoto::getSortIndex)
                                .thenComparing(RentalPhoto::getId)
                )
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "url", "/rentals/photos/" + p.getId(),
                        "filename", p.getFilename(),
                        "sortIndex", p.getSortIndex()
                ))
                .collect(Collectors.toList());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", r.getId());
        payload.put("title", r.getTitle());
        payload.put("price", r.getPrice());
        // IMPORTANT: use location safely (getCity() may throw)
        payload.put("location", r.getLocation());
        payload.put("createdAt", r.getCreatedAt());
        payload.put("photos", photos);
        payload.put(
                "firstPhotoUrl",
                photos.isEmpty() ? null : photos.get(0).get("url")
        );

        return ResponseEntity.ok(payload);
    }

    // NOTE:
    // We purposely do NOT expose @GetMapping("/photos/{photoId}") here
    // to avoid any path clashes with other controllers.
}
