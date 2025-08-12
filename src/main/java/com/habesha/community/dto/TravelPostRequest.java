package com.habesha.community.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request payload for creating a new travel post.  Users share
 * upcoming trips so others can coordinate with them.  A disclaimer
 * should be displayed on the frontend reminding users to use
 * caution when meeting strangers.
 */
@Data
public class TravelPostRequest {
    @NotBlank
    private String originCity;
    @NotBlank
    private String destinationCity;
    @NotNull
    @FutureOrPresent
    private LocalDate travelDate;
    private String message;
    @NotBlank
    private String contactMethod;
}