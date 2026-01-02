package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for resetting a forgotten password.  Both the reset
 * {@code token} and the new password must be provided.  The token
 * must match a token previously generated via the forgot‑password
 * endpoint.
 */
@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;
    @NotBlank
    private String newPassword;
}