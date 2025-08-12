package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request body for sending an SMS directly to a phone number.  The
 * phone number must be in E.164 format (e.g. +441234567890).
 */
@Data
public class SmsRequest {
    @NotBlank
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone number must be in international format")
    private String toNumber;
    @NotBlank
    private String message;
}