// src/main/java/com/habesha/community/controller/ApiFriendController.java
package com.habesha.community.controller;

import com.habesha.community.dto.FriendRequestActionRequest;
import com.habesha.community.dto.FriendRequestCreateRequest;
import com.habesha.community.dto.FriendRequestResponse;
import com.habesha.community.dto.PagedResponse;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.FriendRelationshipStatus;
import com.habesha.community.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API endpoints for managing friendships.
 * Request/accept/decline/cancel, remove friend, status, suggestions.
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class ApiFriendController {

    private final FriendService friendService;

    /** List current user’s friends as summaries (optionally paged). */
    @GetMapping("/me")
    public ResponseEntity<List<UserSummaryDto>> listMyFriends(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size) {

        PagedResponse<UserSummaryDto> paged = friendService.getFriendsPage(page, size);
        return ResponseEntity.ok(paged.getContent());
    }

    /** Relationship status between me and the given user. */
    @GetMapping("/status/{userId}")
    public ResponseEntity<FriendRelationshipStatus> getStatus(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getRelationshipStatus(userId));
    }

    /** Send a friend request to {userId}. */
    @PostMapping("/request/{userId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long userId) {
        try {
            FriendRequestCreateRequest req = new FriendRequestCreateRequest();
            req.setReceiverId(userId);
            friendService.sendRequest(req);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /** Accept a pending request from {userId}. */
    @PostMapping("/accept/{userId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long userId) {
        var incoming = friendService.getIncomingRequests().stream()
                .filter(fr -> fr.getSenderId().equals(userId))
                .findFirst();
        if (incoming.isEmpty()) {
            return ResponseEntity.badRequest().body("No pending request from user");
        }
        FriendRequestActionRequest action = new FriendRequestActionRequest();
        action.setRequestId(incoming.get().getId());
        action.setAccept(true);
        friendService.respondToRequest(action);
        return ResponseEntity.ok().build();
    }

    /** Decline a pending request from {userId}. */
    @PostMapping("/decline/{userId}")
    public ResponseEntity<?> declineRequest(@PathVariable Long userId) {
        var incoming = friendService.getIncomingRequests().stream()
                .filter(fr -> fr.getSenderId().equals(userId))
                .findFirst();
        if (incoming.isEmpty()) {
            return ResponseEntity.badRequest().body("No pending request from user");
        }
        FriendRequestActionRequest action = new FriendRequestActionRequest();
        action.setRequestId(incoming.get().getId());
        action.setAccept(false);
        friendService.respondToRequest(action);
        return ResponseEntity.ok().build();
    }

    /** Remove/unfriend {userId}. Idempotent. */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId) {
        friendService.removeFriend(userId);
        return ResponseEntity.noContent().build();
    }

    /** Incoming pending requests (limited). */
    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestResponse>> getIncomingRequests(
            @RequestParam(name = "limit", required = false) Integer limit) {
        int lim = (limit == null || limit < 1) ? 5 : limit;
        var incoming = friendService.getIncomingRequests();
        if (incoming.size() > lim) incoming = incoming.subList(0, lim);
        return ResponseEntity.ok(incoming);
    }

    /** People you may know (ranked by mutuals). */
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSummaryDto>> getSuggestions(
            @RequestParam(name = "limit", required = false) Integer limit) {
        int lim = (limit == null || limit < 1) ? 6 : limit;
        var suggestions = friendService.getFriendSuggestions(lim); // ← matches service signature
        return ResponseEntity.ok(suggestions);
    }
}
