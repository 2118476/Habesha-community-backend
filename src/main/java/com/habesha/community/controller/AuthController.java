package com.habesha.community.controller;

import com.habesha.community.dto.AuthenticationResponse;
import com.habesha.community.dto.LoginRequest;
import com.habesha.community.dto.RegisterRequest;
import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.User;
import com.habesha.community.service.AuthenticationService;
import com.habesha.community.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        return userService.getCurrentUser()
                .map(userService::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}