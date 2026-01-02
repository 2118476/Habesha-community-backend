package com.habesha.community.controller;

import com.habesha.community.dto.ForgotPasswordRequest;
import com.habesha.community.dto.ResetPasswordRequest;
import com.habesha.community.model.PasswordResetToken;
import com.habesha.community.model.User;
import com.habesha.community.repository.PasswordResetTokenRepository;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.MailService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller providing password reset flows with email functionality.
 * When a user requests a password reset, a secure token is generated
 * and stored with expiration time, then emailed to the user.
 * The token can be used once to reset the password within 30 minutes.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * Initiate a password reset by generating a secure token and sending
     * it via email. Always returns 200 to prevent user enumeration attacks.
     */
    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            
            if (user != null) {
                // Clean up any existing tokens for this user
                tokenRepository.deleteByUser(user);
                
                // Generate secure token
                String token = UUID.randomUUID().toString().replace("-", "");
                
                // Create token entity with 30-minute expiration
                PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .used(false)
                    .build();
                
                tokenRepository.save(resetToken);
                
                // Send email
                mailService.sendPasswordResetEmail(user.getEmail(), token);
                
                log.info("Password reset token generated and email sent for user: {}", user.getEmail());
            } else {
                log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            }
            
            // Always return success to prevent user enumeration
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Reset a user's password using a valid token.
     * The token must exist, not be expired, and not be used.
     */
    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElse(null);
            
            if (resetToken == null || !resetToken.isValid()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid or expired reset token");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = resetToken.getUser();
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            
            log.info("Password successfully reset for user: {}", user.getEmail());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing password reset", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "An error occurred while resetting password");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}