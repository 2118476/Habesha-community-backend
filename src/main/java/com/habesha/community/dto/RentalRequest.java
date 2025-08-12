package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Payload used by users to create a new rental listing.  Images
 * should be provided as a list of URL strings referencing uploaded
 * files.  The frontend handles actual file uploads and returns
 * accessible URLs to the backend.
 */
@Data
public class RentalRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String location;
    @NotNull
    @Positive
    private BigDecimal price;
    @NotBlank
    private String roomType;
    @NotBlank
    private String contact;
    private List<String> images;
    private boolean featured;
}