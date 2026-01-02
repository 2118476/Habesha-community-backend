package com.habesha.community.controller;

import com.habesha.community.dto.AdminUserDto;
import com.habesha.community.model.Role;
import com.habesha.community.service.UserAdminService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Administrative user management API. Only users with the ADMIN
 * role may access these endpoints. Provides paginated listing
 * of users and allows changing a user's role.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<Page<AdminUserDto>> listUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) String query
    ) {
        Page<AdminUserDto> result = userAdminService.listUsers(query, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve a single user by id.  Returns a full {@link AdminUserDto}
     * including last login and activity timestamps.  Only admins may
     * access this endpoint.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userAdminService.getUserById(id));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role
    ) {
        Role newRole;
        try {
            newRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        userAdminService.updateRole(id, newRole);
        return ResponseEntity.ok().build();
    }

    /**
     * Permanently delete a user.  Use with caution: this removes the
     * user record from the database and may orphan related data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userAdminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deactivate a user account.  A deactivated user cannot log in or
     * access the API.  This call mirrors the functionality in
     * {@link com.habesha.community.controller.AdminController#deactivateUser(Long)}
     * but is exposed under the /api/admin/users route for consistency.
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userAdminService.setActive(id, false);
        return ResponseEntity.ok().build();
    }

    /**
     * Activate a previously deactivated user account.
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userAdminService.setActive(id, true);
        return ResponseEntity.ok().build();
    }
}