package com.habesha.community.controller;

import com.habesha.community.model.Rental;
import com.habesha.community.model.RentalPhoto;
import com.habesha.community.repository.RentalPhotoRepository;
import com.habesha.community.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RentalPhotoController {

    private final RentalRepository rentalRepository;
    private final RentalPhotoRepository photoRepository;

    @GetMapping("/rentals/photos/{photoId}")
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) {
        RentalPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/rentals/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) {
        Rental r = rentalRepository.findById(id).orElse(null);
        if (r == null || r.getPhotos() == null || r.getPhotos().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return stream(r.getPhotos().get(0));
    }

    private ResponseEntity<byte[]> stream(RentalPhoto p) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());

        // Try disk first
        try {
            Path path = Path.of(p.getFilePath());
            if (Files.exists(path)) {
                String mime = Files.probeContentType(path);
                if (mime == null) mime = "image/jpeg";
                return ResponseEntity.ok()
                        .headers(headers)
                        .contentType(MediaType.parseMediaType(mime))
                        .body(Files.readAllBytes(path));
            }
        } catch (Exception ignored) {}

        // Fallback to database blob
        if (p.getImageData() != null && p.getImageData().length > 0) {
            String mime = p.getContentType() != null ? p.getContentType() : "image/jpeg";
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(mime))
                    .body(p.getImageData());
        }

        return ResponseEntity.notFound().build();
    }
}
