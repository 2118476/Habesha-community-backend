package com.habesha.community.controller;

import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.UserSessionService;
import com.habesha.community.service.AccountDeletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller for account management operations like freeze/reactivate/delete
 */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final AccountDeletionService accountDeletionService;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Not authenticated");
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * Freeze the current user's account
     */
    @PostMapping("/freeze")
    public ResponseEntity<Map<String, Object>> freezeAccount() {
        User user = getCurrentUserOrThrow();
        
        user.setFrozen(true);
        user.setFrozenAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Optionally revoke all other sessions
        try {
            userSessionService.signOutAllOtherSessions();
        } catch (Exception e) {
            // Continue even if session cleanup fails
        }
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "frozen", true,
            "message", "Account frozen successfully"
        ));
    }

    /**
     * Reactivate a frozen account
     */
    @PostMapping("/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateAccount() {
        User user = getCurrentUserOrThrow();
        
        user.setFrozen(false);
        user.setFrozenAt(null);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "frozen", false,
            "message", "Account reactivated successfully"
        ));
    }

    /**
     * Request account deletion (creates a request for admin/moderator review)
     */
    @PostMapping("/delete-request")
    public ResponseEntity<Map<String, Object>> requestAccountDeletion() {
        User user = getCurrentUserOrThrow();
        
        var request = accountDeletionService.createDeletionRequest(user);
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "requestId", request.getId(),
            "status", request.getStatus().toString(),
            "message", "Account deletion request submitted for review"
        ));
    }
}