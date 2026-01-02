package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Full user profile sent to the frontend.  Sensitive fields such as
 * passwords are never exposed.  Depending on the caller, certain
 * fields (e.g. email) may be omitted for privacy.  Counts reflect
 * the number of related records owned by the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String displayName;
    private String username;
    private String email;
    private String location;
    private String bio;
    private String avatarUrl;
    private String bannerUrl;
    /**
     * Total experience points earned by the user in the learning
     * module.  Exposed to enable leaderboards and progress bars in
     * the frontend.
     */
    private Integer xp;

    /**
     * List of badge identifiers selected by the user.  The order of
     * the list reflects the positions on their profile; positions
     * without a badge will be null.
     */
    private java.util.List<String> badges;

    /**
     * Social media links carried over from the E‑Learning profile.
     */
    private String twitter;
    private String linkedin;
    private String instagram;
    private String joinDate;
    private Long friendsCount;
    private Long eventsCount;
    private Long servicesCount;
    private Long rentalsCount;
}