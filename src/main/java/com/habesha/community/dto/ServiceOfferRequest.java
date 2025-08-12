package com.habesha.community.dto;

import com.habesha.community.model.ServiceMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request used by service providers to create a new service offering
 * on the marketplace.
 */
@Data
public class ServiceOfferRequest {
    @NotBlank
    private String category;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String estimatedTime;
    @NotNull
    @Positive
    private BigDecimal basePrice;
    @NotBlank
    private String location;
    @NotNull
    private ServiceMode mode;
    private boolean featured;
}