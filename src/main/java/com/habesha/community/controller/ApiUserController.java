package com.habesha.community.controller;

import com.habesha.community.dto.UserProfileDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.dto.UserUpdateRequest;
import com.habesha.community.model.User;
import com.habesha.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for working with user profiles.  These endpoints
 * are exposed under the /api/users namespace to clearly delineate
 * between the existing /users routes and the new enterprise‑grade
 * API specified in the sprint brief.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ApiUserController {

    private final UserService userService;
    private final com.habesha.community.service.BlockService blockService;

    /**
     * Get the current authenticated user's full profile.  Returns 401
     * if there is no authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile() {
        return userService.getCurrentUser()
                .map(u -> userService.toProfile(u, true))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build());
    }

    /**
     * Update the current user's profile.  Only the supplied fields
     * will be updated.  Validation errors result in a 400 Bad Request.
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateMyProfile(@Valid @RequestBody UserUpdateRequest request) {
        UserProfileDto updated = userService.updateCurrentUser(
                request.getDisplayName(),
                request.getLocation(),
                request.getAvatarUrl(),
                request.getBannerUrl(),
                request.getBio(),
                request.getTwitter(),
                request.getLinkedin(),
                request.getInstagram()
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * Update a single badge slot for the current user.  The request
     * body should include an integer {@code index} (zero‑based) and
     * a string {@code badgeId} identifying the badge.  Passing a
     * null or empty {@code badgeId} will clear the slot.  Returns
     * the updated user profile.
     */
    @PostMapping("/me/badges")
    public ResponseEntity<UserProfileDto> updateBadge(@RequestBody java.util.Map<String, Object> payload) {
        Object indexObj = payload.get("index");
        Object badgeObj = payload.get("badgeId");
        if (indexObj == null || !(indexObj instanceof Number)) {
            return ResponseEntity.badRequest().build();
        }
        int index = ((Number) indexObj).intValue();
        String badgeId = badgeObj != null ? badgeObj.toString() : null;
        try {
            UserProfileDto updated = userService.updateBadge(index, badgeId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieve a leaderboard of users ordered by experience points (xp).
     * Returns a list of user profiles with xp included, ordered
     * descending by xp.  This endpoint mirrors the functionality
     * provided by the E‑Learning {@code /getRankings} route.
     */
    @GetMapping("/rankings")
    public ResponseEntity<java.util.List<UserProfileDto>> getRankings() {
        // Fetch all users ordered by xp descending via the service
        java.util.List<com.habesha.community.model.User> users = userService.findAllOrderedByXp();
        java.util.List<UserProfileDto> profiles = new java.util.ArrayList<>();
        for (com.habesha.community.model.User u : users) {
            profiles.add(userService.toProfile(u, false));
        }
        return ResponseEntity.ok(profiles);
    }

    /**
     * Get another user's profile by id.  Sensitive fields such as email
     * are omitted for privacy. Returns 404 if blocked or user not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getById(@PathVariable Long id) {
        try {
            // Check if there's a block relationship
            if (blockService.isBlockedByOrBlocking(id)) {
                return ResponseEntity.notFound().build();
            }
            
            User found = userService.getEntityById(id);
            boolean includeEmail = false;
            return ResponseEntity.ok(userService.toProfile(found, includeEmail));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get a user's profile by username/handle.  Sensitive fields are
     * omitted for privacy.  Returns 404 if no user is found or blocked.
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserProfileDto> getByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .filter(u -> {
                    try {
                        return !blockService.isBlockedByOrBlocking(u.getId());
                    } catch (Exception e) {
                        return true; // If not authenticated, allow access
                    }
                })
                .map(u -> userService.toProfile(u, false))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a lightweight summary of a user by id.  Useful when embedding
     * author details inside other responses.  Always returns 404 on
     * failure instead of 500 to avoid leaking entity existence.
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<UserSummaryDto> getSummary(@PathVariable Long id) {
        try {
            // Check if there's a block relationship
            if (blockService.isBlockedByOrBlocking(id)) {
                return ResponseEntity.notFound().build();
            }
            
            User found = userService.getEntityById(id);
            return ResponseEntity.ok(userService.toSummary(found));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}