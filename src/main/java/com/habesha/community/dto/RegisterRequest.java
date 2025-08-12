package com.habesha.community.dto;

import com.habesha.community.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload for registering a new account.  All fields are required
 * except for the profile image URL which is optional.  By default
 * newly registered users are assigned the USER role; specifying a
 * different role requires administrative privileges and should only
 * occur via the admin panel.
 */
@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private String phone;

    @NotBlank
    private String city;

    private String profileImageUrl;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private Role role = Role.USER;
}