package com.habesha.community.controller;

import com.habesha.community.model.HomeSwapPhoto;
import com.habesha.community.repository.HomeSwapPhotoRepository;
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
public class HomeSwapPhotoController {

    private final HomeSwapPhotoRepository photoRepository;

    @GetMapping("/homeswap/photos/{photoId}")
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) {
        HomeSwapPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/homeswap/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) {
        HomeSwapPhoto p = photoRepository.findFirstByHomeSwap_IdOrderBySortOrderAscIdAsc(id)
                .orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    private ResponseEntity<byte[]> stream(HomeSwapPhoto p) {
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
