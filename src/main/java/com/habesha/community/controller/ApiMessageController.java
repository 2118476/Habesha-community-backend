package com.habesha.community.controller;

import com.habesha.community.dto.EnsureThreadRequest;
import com.habesha.community.dto.EnsureThreadResponse;
import com.habesha.community.dto.MessageRequest;
import com.habesha.community.dto.ThreadSummaryDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.Message;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Consolidated, friendship-agnostic messaging controller.
 * IMPORTANT: It serves BOTH /api/messages/* and legacy /messages/* paths,
 * so the existing frontend continues to work without changes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/messages", "/messages"})
public class ApiMessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    /* ----------------------------- utilities ----------------------------- */

    private String resolveAvatarUrl(User u) {
        if (u == null || u.getId() == null) return null;
        // If you persist absolute URLs on User, prefer them here.
        // if (u.getProfileImageUrl() != null && !u.getProfileImageUrl().isBlank()) return u.getProfileImageUrl();
        return "/users/" + u.getId() + "/profile-image";
    }

    /* ------------------------------ endpoints ---------------------------- */

    /**
     * Ensure a DM target exists (no friendship requirement).
     * Frontend calls POST /api/messages/ensure-thread
     */
    @PostMapping("/ensure-thread")
    public ResponseEntity<EnsureThreadResponse> ensureThread(
            @RequestBody(required = false) EnsureThreadRequest body,
            @RequestParam(value = "targetUserId", required = false) Long targetUserId
    ) {
        Long id = (body != null && body.getUserId() != null) ? body.getUserId() : targetUserId;
        if (id == null) return ResponseEntity.badRequest().build();

        User target = userRepository.findById(id).orElse(null);
        if (target == null) return ResponseEntity.notFound().build();

        UserSummaryDto to = UserSummaryDto.builder()
                .id(target.getId())
                .displayName(
                        (target.getName() != null && !target.getName().isBlank())
                                ? target.getName()
                                : (target.getUsername() != null && !target.getUsername().isBlank()
                                ? target.getUsername() : target.getEmail())
                )
                .username(target.getUsername())
                .avatarUrl(resolveAvatarUrl(target))
                .build();

        return ResponseEntity.ok(
                EnsureThreadResponse.builder()
                        .threadUserId(target.getId())
                        .to(to)
                        .build()
        );
    }

    /**
     * Send a message (OPEN DMs). Supports both:
     *  - POST /api/messages/send
     *  - POST /messages/send   (legacy used by Messages.jsx)
     */
    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestBody MessageRequest request) {
        messageService.sendMessage(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get conversation with another user (ascending by time).
     * Supports both:
     *  - GET /api/messages/conversation/{otherUserId}
     *  - GET /messages/{otherUserId}  (legacy used by Messages.jsx)
     */
    @GetMapping({"/conversation/{otherUserId}", "/{otherUserId}"})
    public ResponseEntity<List<Message>> conversation(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(messageService.getConversation(otherUserId));
    }

    /**
     * Mark messages from {otherUserId} as read.
     * Supports both:
     *  - POST /api/messages/mark-read/{otherUserId}
     *  - POST /messages/read/{otherUserId}   (legacy used by Messages.jsx)
     */
    @PostMapping({"/mark-read/{otherUserId}", "/read/{otherUserId}"})
    public ResponseEntity<Void> markRead(@PathVariable Long otherUserId) {
        messageService.markReadFromOther(otherUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Recent distinct threads for sidebar.
     * Uses: GET /api/messages/threads  (current UI)
     * Also available at: GET /messages/threads
     */
    @GetMapping("/threads")
    public ResponseEntity<List<ThreadSummaryDto>> threads(@RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(messageService.getRecentThreads(limit));
    }

    /**
     * Total unread count for the current user.
     * Supports both:
     *  - GET /api/messages/unread-count
     *  - GET /messages/unread-count   (used by hooks/useUnreadCount.js)
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        return ResponseEntity.ok(messageService.unreadCountForCurrentUser());
    }

    /* -------------------------- friendly errors -------------------------- */

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
