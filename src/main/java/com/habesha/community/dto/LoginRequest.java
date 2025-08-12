package com.habesha.community.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Payload for authenticating a user.  Requires an email and a
 * password.  A successful login returns a JWT token in the
 * response body.
 */
@Data
public class LoginRequest {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}