package com.habesha.community.service;

import com.habesha.community.dto.AuthenticationResponse;
import com.habesha.community.dto.LoginRequest;
import com.habesha.community.dto.RegisterRequest;
import com.habesha.community.model.EmailVerificationToken;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.EmailVerificationTokenRepository;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login.  When a new user registers their
 * password is hashed using BCrypt.  On successful authentication a
 * signed JWT token is returned for subsequent requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final MailService mailService;

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
                .username(request.getEmail().substring(0, request.getEmail().indexOf('@')))
                .phone(request.getPhone())
                .city(request.getCity())
                .profileImageUrl(request.getProfileImageUrl())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .active(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);

        // Create verification token and send email
        String token = java.util.UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(java.time.LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        mailService.sendEmailVerification(user.getEmail(), token);

        String jwtToken = jwtService.generateToken(user);
        userSessionService.createOrUpdateSession(user, jwtToken);

        return new AuthenticationResponse(jwtToken, userService.toResponse(user));
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (!verificationToken.isValid()) {
            throw new IllegalArgumentException("Verification link has expired or already been used");
        }

        verificationToken.setUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new IllegalArgumentException("Email is already verified");
        }

        String token = java.util.UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(java.time.LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);
        mailService.sendEmailVerification(user.getEmail(), token);
    }

    public AuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw e; // Let GlobalExceptionHandler return 401
        } catch (org.springframework.security.authentication.InternalAuthenticationServiceException e) {
            log.error("Internal authentication error for email={}: {}", request.getEmail(), e.getMessage(), e);
            throw new org.springframework.security.authentication.BadCredentialsException("Authentication failed", e);
        }

        // Re-load managed entity so save() works on a clean JPA-managed instance
        User principal = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        log.info("Login authenticated successfully for email={}, userId={}, role={}",
                principal.getEmail(), principal.getId(), principal.getRole());

        String token;
        try {
            token = jwtService.generateToken(principal);
        } catch (Exception e) {
            log.error("Failed to generate JWT token for email={}: {}", principal.getEmail(), e.getMessage(), e);
            throw new IllegalStateException("Failed to generate authentication token. Please contact support.");
        }

        try {
            principal.setLastLoginAt(java.time.LocalDateTime.now());
            principal.setLastActiveAt(java.time.LocalDateTime.now());
            userRepository.save(principal);
            log.info("Updated login timestamps for email={}", principal.getEmail());
        } catch (Exception e) {
            log.warn("Login succeeded but failed to persist login timestamps for email={}: {}",
                    principal.getEmail(), e.getMessage(), e);
        }

        try {
            userSessionService.createOrUpdateSession(principal, token);
            log.info("Created/updated session for email={}", principal.getEmail());
        } catch (Exception e) {
            log.warn("Login succeeded but failed to persist session for email={}: {}",
                    principal.getEmail(), e.getMessage(), e);
        }

        return new AuthenticationResponse(token, userService.toResponse(principal));
    }
}