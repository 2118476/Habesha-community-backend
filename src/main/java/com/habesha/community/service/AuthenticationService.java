package com.habesha.community.service;

import com.habesha.community.dto.AuthenticationResponse;
import com.habesha.community.dto.LoginRequest;
import com.habesha.community.dto.RegisterRequest;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.  When a new user registers their
 * password is hashed using BCrypt.  On successful authentication a
 * signed JWT token is returned for subsequent requests.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;

    // Provide access to user conversions so we can include a UserResponse
    // in the authentication response.
    private final com.habesha.community.service.UserService userService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Prevent duplicate registration
        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Email already registered");
        });
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .profileImageUrl(request.getProfileImageUrl())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() == null ? Role.USER : request.getRole())
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        
        // Create session for this login
        userSessionService.createOrUpdateSession(user, token);
        
        return new AuthenticationResponse(token, userService.toResponse(user));
    }

    public AuthenticationResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        User principal = (User) authentication.getPrincipal();
        // Record the timestamp of this successful login and mark the
        // user as active.  Persist the updates before issuing the JWT.
        principal.setLastLoginAt(java.time.LocalDateTime.now());
        principal.setLastActiveAt(java.time.LocalDateTime.now());
        userRepository.save(principal);
        String token = jwtService.generateToken(principal);
        
        // Create session for this login
        userSessionService.createOrUpdateSession(principal, token);
        
        return new AuthenticationResponse(token, userService.toResponse(principal));
    }
}