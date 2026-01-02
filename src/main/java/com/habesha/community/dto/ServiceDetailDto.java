package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for a service offer detail.  Mirrors the existing
 * ServiceOffer entity while hiding sensitive provider fields and
 * including a summary of the author.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailDto {
    private Long id;
    private String category;
    private String title;
    private String description;
    private String estimatedTime;
    private BigDecimal price;
    private String rateUnit;
    private String location;
    private List<String> tags;
    private boolean featured;
    private LocalDateTime createdAt;
    /**
     * Summary of the user who posted this service offer.  Clients
     * should prefer this field over the now deprecated {@code author}
     * property.
     */
    private UserSummaryDto postedBy;

    /**
     * @deprecated use {@link #postedBy} instead.  Retained for
     * backwards compatibility with existing clients.
     */
    @Deprecated
    private UserSummaryDto author;
}