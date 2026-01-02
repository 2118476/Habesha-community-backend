package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight representation of a user suitable for embedding in
 * other resources.  Exposes only non‑sensitive fields and a
 * "verified" flag which can be used in the frontend to show
 * verification badges.  Counts are optional and may be null.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long id;
    private String displayName;
    private String username;
    private String avatarUrl;
    private boolean verified;
    private Long friendsCount;
    private Long postsCount;
    private Integer mutualCount;
}