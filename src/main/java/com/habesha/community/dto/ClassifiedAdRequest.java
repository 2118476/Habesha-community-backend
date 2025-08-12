package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payload used by users to create a new classified advertisement.
 */
@Data
public class ClassifiedAdRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    @Positive
    private BigDecimal price;
    private String imageUrl;
    @NotBlank
    private String category;
    private boolean featured;
}