package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Request body for changing the current user's password. */
@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
}
