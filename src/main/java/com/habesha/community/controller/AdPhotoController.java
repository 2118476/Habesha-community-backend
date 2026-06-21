package com.habesha.community.controller;

import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.AdPhoto;
import com.habesha.community.repository.AdPhotoRepository;
import com.habesha.community.repository.ClassifiedAdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
    @Transactional(readOnly = true)
    public ResponseEntity<?> photoById(@PathVariable Long photoId) {
        AdPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/ads/{id}/photos/first")
    @Transactional(readOnly = true)
    public ResponseEntity<?> firstPhoto(@PathVariable Long id) {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || ad.getPhotos().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(0));
    }

    @GetMapping("/ads/{id}/photos/{index}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> photoByIndex(@PathVariable Long id, @PathVariable Integer index) {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || index >= ad.getPhotos().size() || index < 0) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(index));
    }

    private ResponseEntity<?> stream(AdPhoto p) {
        // CDN redirect for Supabase-stored photos
        String fp = p.getFilePath();
        if (fp != null && (fp.startsWith("http://") || fp.startsWith("https://"))) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(fp))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());

        // Try disk first
        try {
            Path path = Path.of(fp);
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
