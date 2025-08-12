package com.habesha.community.controller;

import com.habesha.community.dto.ClassifiedAdRequest;
import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.service.AdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints for classified advertisements.
 */
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class AdController {
    private final AdService adService;

    @PostMapping
    public ResponseEntity<ClassifiedAd> createAd(@Valid @RequestBody ClassifiedAdRequest request) {
        return ResponseEntity.ok(adService.createAd(request));
    }

    @GetMapping
    public ResponseEntity<List<ClassifiedAd>> listAds(@RequestParam(name = "category", required = false) String category) {
        return ResponseEntity.ok(adService.listAds(Optional.ofNullable(category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.noContent().build();
    }
}