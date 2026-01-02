package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for a rental detail.  Contains the rental's
 * properties along with a summary of the owner.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalDetailDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String currency;
    private BigDecimal deposit;
    private String roomType;
    private String location;
    private List<String> amenities;
    private boolean featured;
    private List<String> images;
    private LocalDateTime createdAt;
    /**
     * Summary of the user who posted this rental listing.  Use this
     * property instead of the deprecated {@code author} field.
     */
    private UserSummaryDto postedBy;

    /**
     * @deprecated use {@link #postedBy} instead.  Kept for backwards
     * compatibility; will be removed in a future version.
     */
    @Deprecated
    private UserSummaryDto author;
}