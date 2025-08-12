package com.habesha.community.controller;

import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for retrieving user information. Only admins can delete
 * other users.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Get a specific user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Get all users by role, or default to service providers.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsersByRole(@RequestParam(name = "role", required = false) Role role) {
        if (role == null) {
            return ResponseEntity.ok(userService.getUsersByRole(Role.SERVICE_PROVIDER));
        }
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Delete a user (admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the currently logged-in user profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return userService.getCurrentUser()
                .map(userService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(401).build()); // not authenticated
    }

    /**
     * Update the current user's profile image.  Accepts a JSON body
     * containing a "profileImageUrl" property which may be a data
     * URI or a remote URL.  Returns the updated UserResponse.
     */
    @PostMapping("/me/profile-image")
    public ResponseEntity<UserResponse> updateProfileImage(@RequestBody java.util.Map<String, String> body) {
        String url = body.get("profileImageUrl");
        return ResponseEntity.ok(userService.updateProfileImage(url));
    }
}
