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

    /** Stream by photo id */
    @GetMapping("/ads/photos/{photoId}")
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) throws Exception {
        AdPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    /** First photo for an ad (thumbnail) */
    @GetMapping("/ads/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) throws Exception {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || ad.getPhotos().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(0)); // list is already ordered in entity
    }

    /** Photo by index for an ad */
    @GetMapping("/ads/{id}/photos/{index}")
    public ResponseEntity<byte[]> photoByIndex(@PathVariable Long id, @PathVariable Integer index) throws Exception {
        ClassifiedAd ad = adRepository.findById(id).orElse(null);
        if (ad == null || ad.getPhotos() == null || index >= ad.getPhotos().size() || index < 0) {
            return ResponseEntity.notFound().build();
        }
        return stream(ad.getPhotos().get(index));
    }

    private ResponseEntity<byte[]> stream(AdPhoto p) throws Exception {
        Path path = Path.of(p.getFilePath());
        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        String mime = Files.probeContentType(path);
        if (mime == null) mime = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(mime))
                .body(Files.readAllBytes(path));
    }
}