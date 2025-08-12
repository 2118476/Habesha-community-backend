package com.habesha.community.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Payload for creating an event.  Events can be promoted to
 * featured status via a separate payment flow.
 */
@Data
public class EventRequest {
    @NotBlank
    private String title;
    @NotNull
    @FutureOrPresent
    private LocalDate date;
    @NotBlank
    private String description;
    private String imageUrl;
    @NotBlank
    private String location;
    private boolean featured;
}