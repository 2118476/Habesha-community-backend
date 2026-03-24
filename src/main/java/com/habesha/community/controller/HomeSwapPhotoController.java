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
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) throws Exception {
        HomeSwapPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    @GetMapping("/homeswap/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) throws Exception {
        HomeSwapPhoto p = photoRepository.findFirstByHomeSwap_IdOrderBySortOrderAscIdAsc(id)
                .orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    private ResponseEntity<byte[]> stream(HomeSwapPhoto p) throws Exception {
        Path path = Path.of(p.getPath());
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
