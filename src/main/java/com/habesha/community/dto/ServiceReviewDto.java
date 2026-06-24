package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** A single service review. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReviewDto {
    private Long id;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private UserSummaryDto reviewer;
}
