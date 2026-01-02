package com.habesha.community.controller;

import com.habesha.community.model.AccountDeletionRequest;
import com.habesha.community.model.AccountDeletionRequest.AccountDeletionStatus;
import com.habesha.community.model.User;
import com.habesha.community.repository.AccountDeletionRequestRepository;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Admin/Moderator controller for managing account deletion requests
 */
@RestController
@RequiredArgsConstructor
public class DeletionRequestAdminController {

    private final AccountDeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Not authenticated");
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * List deletion requests (for admins and moderators)
     */
    @GetMapping("/api/mod/deletion-requests")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Page<AccountDeletionRequest>> listDeletionRequests(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        AccountDeletionStatus statusEnum;
        try {
            statusEnum = AccountDeletionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            statusEnum = AccountDeletionStatus.PENDING;
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AccountDeletionRequest> requests = deletionRequestRepository.findByStatusOrderByCreatedAtDesc(statusEnum, pageable);
        
        return ResponseEntity.ok(requests);
    }

    /**
     * Approve a deletion request
     */
    @PutMapping("/api/mod/deletion-requests/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> approveDeletionRequest(@PathVariable Long id) {
        User currentUser = getCurrentUserOrThrow();
        
        AccountDeletionRequest request = deletionRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found"));
        
        request.setStatus(AccountDeletionStatus.APPROVED);
        request.setHandledAt(LocalDateTime.now());
        request.setHandledByUserId(currentUser.getId());
        deletionRequestRepository.save(request);
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "status", "APPROVED",
            "message", "Deletion request approved"
        ));
    }

    /**
     * Reject a deletion request
     */
    @PutMapping("/api/mod/deletion-requests/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> rejectDeletionRequest(@PathVariable Long id) {
        User currentUser = getCurrentUserOrThrow();
        
        AccountDeletionRequest request = deletionRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found"));
        
        request.setStatus(AccountDeletionStatus.REJECTED);
        request.setHandledAt(LocalDateTime.now());
        request.setHandledByUserId(currentUser.getId());
        deletionRequestRepository.save(request);
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "status", "REJECTED",
            "message", "Deletion request rejected"
        ));
    }

    /**
     * Execute account deletion (ADMIN only)
     */
    @DeleteMapping("/api/admin/deletion-requests/{id}/execute-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> executeDeletion(@PathVariable Long id) {
        User currentUser = getCurrentUserOrThrow();
        
        AccountDeletionRequest request = deletionRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found"));
        
        if (request.getStatus() != AccountDeletionStatus.APPROVED) {
            throw new IllegalStateException("Can only delete approved requests");
        }
        
        // Execute the deletion using existing user service
        Long userIdToDelete = request.getUser().getId();
        userService.deleteUser(userIdToDelete);
        
        // Mark request as completed
        request.setStatus(AccountDeletionStatus.COMPLETED);
        request.setHandledAt(LocalDateTime.now());
        request.setHandledByUserId(currentUser.getId());
        deletionRequestRepository.save(request);
        
        return ResponseEntity.ok(Map.of(
            "ok", true,
            "status", "COMPLETED",
            "message", "Account deleted successfully"
        ));
    }
}