package com.habesha.community.controller;

import com.habesha.community.dto.UserSessionDto;
import com.habesha.community.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Session management endpoints for the Settings > Security page.
 * Allows users to view and manage their active sessions.
 */
@RestController
@RequestMapping("/api/users/me/sessions")
@RequiredArgsConstructor
public class UserSessionController {
    private final UserSessionService sessionService;

    /**
     * Get all active sessions for the current user
     */
    @GetMapping
    public ResponseEntity<List<UserSessionDto>> listSessions() {
        return ResponseEntity.ok(sessionService.getMyActiveSessions());
    }

    /**
     * Sign out a specific session
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> revokeSession(@PathVariable Long id) {
        sessionService.signOutSession(id);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Session signed out"));
    }

    /**
     * Sign out all other sessions (keep current one)
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> revokeAllOtherSessions() {
        sessionService.signOutAllOtherSessions();
        return ResponseEntity.ok(Map.of("ok", true, "message", "All other sessions signed out"));
    }
}
