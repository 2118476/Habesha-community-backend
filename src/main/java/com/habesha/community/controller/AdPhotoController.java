package com.habesha.community.controller;

import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.AdPhoto;
import com.habesha.community.repository.AdPhotoRepository;
import com.habesha.community.repository.ClassifiedAdRepository;
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
public class AdPhotoController {

    private final ClassifiedAdRepository adRepository;
    private final AdPhotoRepository photoRepository;

    @GetMapping("/ads/photos/{photoId}")
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) {
        AdPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/ads/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || ad.getPhotos().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(0));
    }

    @GetMapping("/ads/{id}/photos/{index}")
    public ResponseEntity<byte[]> photoByIndex(@PathVariable Long id, @PathVariable Integer index) {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || index >= ad.getPhotos().size() || index < 0) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(index));
    }

    private ResponseEntity<byte[]> stream(AdPhoto p) {
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
