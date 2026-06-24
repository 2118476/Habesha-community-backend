package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Reviews for a provider plus the current viewer's review state. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReviewSummaryDto {
    private Long providerId;
    private double average;            // 0 when there are no reviews
    private long count;
    private boolean canReview;         // is the current user allowed to leave/update a review
    private String reason;             // human-readable reason when canReview is false
    private ServiceReviewDto myReview; // the current user's existing review, or null
    private List<ServiceReviewDto> reviews;
}
