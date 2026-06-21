package com.habesha.community.controller;

import com.habesha.community.model.HomeSwapPhoto;
import com.habesha.community.repository.HomeSwapPhotoRepository;
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
public class HomeSwapPhotoController {

    private final HomeSwapPhotoRepository photoRepository;

    @GetMapping("/homeswap/photos/{photoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> photoById(@PathVariable Long photoId) {
        HomeSwapPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/homeswap/{id}/photos/first")
    @Transactional(readOnly = true)
    public ResponseEntity<?> firstPhoto(@PathVariable Long id) {
        HomeSwapPhoto p = photoRepository.findFirstByHomeSwap_IdOrderBySortOrderAscIdAsc(id)
                .orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    private ResponseEntity<?> stream(HomeSwapPhoto p) {
        // CDN redirect for Supabase-stored photos (url field holds the public URL)
        String url = p.getUrl();
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(url))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());

        // Try disk first
        try {
            Path path = Path.of(p.getPath());
            if (Files.exists(path)) {
                String mime = Files.probeContentType(path);
                if (mime == null) mime = p.getContentType() != null ? p.getContentType() : "image/jpeg";
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
