package com.habesha.community.controller;

import com.habesha.community.dto.*;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.FriendService;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints for sending and managing friend requests.
 */
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Send a new friend request
     */
    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody FriendRequestCreateRequest request) {
        try {
            friendService.sendRequest(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send friend request");
        }
    }

    /**
     * Accept or reject a friend request
     */
    @PostMapping("/respond")
    public ResponseEntity<?> respondToRequest(@RequestBody FriendRequestActionRequest action) {
        try {
            friendService.respondToRequest(action);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to respond to friend request");
        }
    }

    /**
     * Get a list of the current user's friends
     */
    @GetMapping("/list")
    public ResponseEntity<?> listFriends() {
        try {
            List<SimpleUserDTO> friends = friendService.getFriends();
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retrieving friends");
        }
    }

    /**
     * Get users by partial match on name or username
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam("query") String query) {
        try {
            if (query == null || query.trim().length() < 2) {
                return ResponseEntity.badRequest().body("Search query must be at least 2 characters");
            }

            List<User> matches = userRepository
                    .findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);

            List<SimpleUserDTO> results = matches.stream()
                    .map(user -> new SimpleUserDTO(user.getId(), user.getName(), user.getEmail()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error searching users");
        }
    }

    /**
     * Get incoming friend requests
     */
    @GetMapping("/requests/incoming")
    public ResponseEntity<?> getIncomingRequests() {
        try {
            return ResponseEntity.ok(friendService.getIncomingRequests());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching incoming requests");
        }
    }

    /**
     * Get outgoing friend requests
     */
    @GetMapping("/requests/outgoing")
    public ResponseEntity<?> getOutgoingRequests() {
        try {
            return ResponseEntity.ok(friendService.getOutgoingRequests());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching outgoing requests");
        }
    }
}
