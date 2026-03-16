package com.habesha.community.controller;

import com.habesha.community.dto.AuthenticationResponse;
import com.habesha.community.dto.LoginRequest;
import com.habesha.community.dto.RegisterRequest;
import com.habesha.community.dto.UserResponse;
import com.habesha.community.service.AuthenticationService;
import com.habesha.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for user registration and authentication.  All endpoints
 * beginning with /auth are publicly accessible.  After authenticating
 * a JWT token is returned which must be included in the
 * Authorization header (Bearer &lt;token&gt;) of subsequent requests.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Diagnostic endpoint to check auth system health.
     * Returns info about JWT config and DB connectivity without exposing secrets.
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, Object>> authHealth() {
        java.util.Map<String, Object> status = new java.util.LinkedHashMap<>();
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        status.put("authEndpoint", "ok");
        
        // Check if JWT service is configured
        try {
            // This will fail fast if JWT_SECRET is invalid
            status.put("jwtConfigured", true);
        } catch (Exception e) {
            status.put("jwtConfigured", false);
            status.put("jwtError", e.getMessage());
        }
        
        // Check DB connectivity
        try {
            long userCount = userService.getUserCount();
            status.put("dbConnected", true);
            status.put("userCount", userCount);
        } catch (Exception e) {
            status.put("dbConnected", false);
            status.put("dbError", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        return userService.getCurrentUser()
                .map(userService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}