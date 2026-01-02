package com.habesha.community.dto;

import com.habesha.community.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight representation of a user for administrative lists.
 * Exposes only non‑sensitive fields and basic status/role
 * information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private boolean active;

    /**
     * The timestamp of the user's last successful login.  This
     * corresponds to {@link com.habesha.community.model.User#lastLoginAt}.
     */
    private LocalDateTime lastLoginAt;

    /**
     * The timestamp of the user's most recent authenticated request.
     * Corresponds to {@link com.habesha.community.model.User#lastActiveAt}.
     */
    private LocalDateTime lastActiveAt;

    /**
     * Derived flag indicating whether the user is considered online.
     * A user is considered online if their lastActiveAt timestamp is
     * within a configurable threshold (e.g. the past 5 minutes).  This
     * field is not persisted but is set when converting entities to
     * DTOs.
     */
    private boolean online;
}