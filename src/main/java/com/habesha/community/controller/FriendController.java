// src/main/java/com/habesha/community/controller/FriendController.java
package com.habesha.community.controller;

import com.habesha.community.dto.*;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.FriendService;
import com.habesha.community.service.UserService;
import com.habesha.community.dto.CancelFriendRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Validated
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;
    private final UserService userService;

    /* ------------ Relationship primitives ------------ */

    /** Current user's friends (paged). */
    @GetMapping("/list")
    public ResponseEntity<PagedResponse<UserSummaryDto>> listFriends(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ResponseEntity.ok(friendService.getFriendsPage(page, size));
    }

    /** Remove (unfriend) a user. */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long userId) {
        friendService.removeFriend(userId);
        return ResponseEntity.ok().build();
    }

    /** Relationship status with a target user. */
    @GetMapping("/status")
    public ResponseEntity<RelationshipStatusResponse> getStatus(@RequestParam("userId") Long userId) {
        return ResponseEntity.ok(friendService.getRelationshipStatusResponse(userId));
    }

    /* ------------ Requests ------------ */

    /** Send a friend request. */
    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody FriendRequestCreateRequest request) {
        friendService.sendRequest(request);
        return ResponseEntity.ok().build();
    }

    /** Accept or reject a request. */
    @PostMapping("/respond")
    public ResponseEntity<?> respondToRequest(@RequestBody FriendRequestActionRequest action) {
        friendService.respondToRequest(action);
        return ResponseEntity.ok().build();
    }

    /** Cancel an outgoing pending request. */
    @PostMapping("/requests/cancel")
    public ResponseEntity<?> cancelOutgoing(@RequestBody CancelFriendRequest request) {
        friendService.cancelOutgoingRequest(request.getRequestId());
        return ResponseEntity.ok().build();
    }

    /** Incoming (received) pending requests (paged). */
    @GetMapping("/requests/incoming")
    public ResponseEntity<PagedResponse<FriendRequestResponse>> incoming(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ResponseEntity.ok(friendService.getIncomingRequestsPage(page, size));
    }

    /** Outgoing (sent) pending requests (paged). */
    @GetMapping("/requests/outgoing")
    public ResponseEntity<PagedResponse<FriendRequestResponse>> outgoing(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ResponseEntity.ok(friendService.getOutgoingRequestsPage(page, size));
    }

    /* ------------ Discovery ------------ */

    /** People you may know (ranked by mutual friends). */
    @GetMapping("/suggestions")
    public ResponseEntity<List<UserSummaryDto>> suggestions(
            @RequestParam(defaultValue = "12") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(friendService.getFriendSuggestions(limit));
    }

    /** Mutual friends between current user and target. */
    @GetMapping("/mutual/{userId}")
    public ResponseEntity<List<UserSummaryDto>> mutual(@PathVariable Long userId) {
        return ResponseEntity.ok(friendService.getMutualFriends(userId));
    }

    /** Search users (by name or username), returns summaries. */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserSummaryDto>> search(
            @RequestParam("query") String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(friendService.searchUsers(query, page, size));
    }
}
