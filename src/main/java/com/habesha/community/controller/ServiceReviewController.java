package com.habesha.community.controller;

import com.habesha.community.dto.ServiceReviewDto;
import com.habesha.community.dto.ServiceReviewSummaryDto;
import com.habesha.community.service.ServiceReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Reviews for a service provider.
 *  GET    -> public (anyone can read reviews + the average)
 *  POST   -> authenticated, gated by a genuine two-way chat (see service)
 *  DELETE -> authenticated, removes the caller's own review
 */
@RestController
@RequestMapping("/api/services/providers/{providerId}/reviews")
@RequiredArgsConstructor
public class ServiceReviewController {

    private final ServiceReviewService reviewService;

    @GetMapping
    public ResponseEntity<ServiceReviewSummaryDto> list(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getSummary(providerId));
    }

    @PostMapping
    public ResponseEntity<ServiceReviewDto> create(
            @PathVariable Long providerId,
            @RequestBody ReviewRequest body) {
        return ResponseEntity.ok(
                reviewService.createOrUpdate(providerId, body.getRating(), body.getComment()));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long providerId) {
        reviewService.deleteMine(providerId);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class ReviewRequest {
        private int rating;
        private String comment;
    }
}
