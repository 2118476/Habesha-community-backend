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
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class RentalPhotoController {

    private final RentalRepository rentalRepository;
    private final RentalPhotoRepository photoRepository;

    /** Stream by photo id (SINGLE OWNER for this route) */
    @GetMapping("/rentals/photos/{photoId}")
    public ResponseEntity<byte[]> photoById(@PathVariable Long photoId) throws Exception {
        RentalPhoto p = photoRepository.findById(photoId).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        return stream(p);
    }

    /** First photo for a rental (thumbnail) */
    @GetMapping("/rentals/{id}/photos/first")
    public ResponseEntity<byte[]> firstPhoto(@PathVariable Long id) throws Exception {
        Rental r = rentalRepository.findById(id).orElse(null);
        if (r == null || r.getPhotos() == null || r.getPhotos().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return stream(r.getPhotos().get(0)); // list is already ordered in entity
    }

    private ResponseEntity<byte[]> stream(RentalPhoto p) throws Exception {
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
