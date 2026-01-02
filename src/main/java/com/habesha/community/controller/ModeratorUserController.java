package com.habesha.community.controller;

import com.habesha.community.dto.AdminUserDto;
import com.habesha.community.service.UserAdminService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * User listing API for moderators and admins. This controller
 * exposes read‑only access to user lists and cannot modify roles.
 */
@RestController
@RequestMapping("/api/mod/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class ModeratorUserController {

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
}