package com.habesha.community.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for updating the current user's profile.  All fields are
 * optional; null values indicate that the corresponding property
 * should remain unchanged.  Validation annotations ensure sensible
 * length limits.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @Size(max = 255)
    private String displayName;
    @Size(max = 255)
    private String location;
    @Size(max = 1024)
    private String avatarUrl;
    @Size(max = 1024)
    private String bannerUrl;
    @Size(max = 1024)
    private String bio;

    /**
     * Optional social media links for the user.  When present these
     * values will replace the existing links stored on the user
     * entity.  Length is limited to avoid excessively long URLs.
     */
    @Size(max = 255)
    private String twitter;
    @Size(max = 255)
    private String linkedin;
    @Size(max = 255)
    private String instagram;
}