package com.habesha.community.controller;

import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.Role;

import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Endpoints for retrieving and updating user information.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
  

    /** Get a specific user by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** Get all users by role, or default to service providers. */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsersByRole(
            @RequestParam(name = "role", required = false) Role role) {
        if (role == null) {
            return ResponseEntity.ok(userService.getUsersByRole(Role.SERVICE_PROVIDER));
        }
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /** Delete a user (admin only). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Get the currently logged-in user profile. */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return userService.getCurrentUser()
                .map(userService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


 
}
