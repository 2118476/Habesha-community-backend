package com.habesha.community.service;

import com.habesha.community.dto.FriendRequestActionRequest;
import com.habesha.community.dto.FriendRequestCreateRequest;
import com.habesha.community.dto.FriendRequestResponse;
import com.habesha.community.dto.SimpleUserDTO;
import com.habesha.community.model.FriendRequest;
import com.habesha.community.model.FriendRequestStatus;
import com.habesha.community.model.User;
import com.habesha.community.repository.FriendRequestRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));
    }

    /**
     * Send a new friend request to another user
     */
    @Transactional
    public void sendRequest(FriendRequestCreateRequest request) {
        User sender = getCurrentUserOrThrow();
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }

        boolean alreadyExists = friendRequestRepository
                .findBySenderAndReceiver(sender, receiver).isPresent()
                || friendRequestRepository.findBySenderAndReceiver(receiver, sender).isPresent();

        if (alreadyExists) {
            throw new IllegalStateException("A friend request already exists between you and this user");
        }

        FriendRequest requestEntity = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(requestEntity);
    }

    /**
     * Accept or reject a pending friend request
     */
    @Transactional
    public void respondToRequest(FriendRequestActionRequest action) {
        User current = getCurrentUserOrThrow();

        FriendRequest request = friendRequestRepository.findById(action.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!request.getReceiver().getId().equals(current.getId())) {
            throw new IllegalStateException("You are not authorized to respond to this request");
        }

        request.setStatus(action.isAccept() ? FriendRequestStatus.ACCEPTED : FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);
    }

    /**
     * Get all accepted friends of the current user
     */
    public List<SimpleUserDTO> getFriends() {
        User current = getCurrentUserOrThrow();

        List<FriendRequest> accepted = friendRequestRepository
                .findBySenderOrReceiverAndStatus(current, current, FriendRequestStatus.ACCEPTED);

        Set<User> friends = new HashSet<>();
        for (FriendRequest fr : accepted) {
            if (fr.getSender().getId().equals(current.getId())) {
                friends.add(fr.getReceiver());
            } else {
                friends.add(fr.getSender());
            }
        }

        return friends.stream()
                .map(user -> new SimpleUserDTO(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Get incoming (received) pending friend requests
     */
    public List<FriendRequestResponse> getIncomingRequests() {
        User current = getCurrentUserOrThrow();
        return friendRequestRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(current, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get outgoing (sent) pending friend requests
     */
    public List<FriendRequestResponse> getOutgoingRequests() {
        User current = getCurrentUserOrThrow();
        return friendRequestRepository
                .findBySenderAndStatusOrderByCreatedAtDesc(current, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert a FriendRequest entity to a response DTO
     */
    private FriendRequestResponse toResponse(FriendRequest fr) {
        return FriendRequestResponse.builder()
                .id(fr.getId())
                .status(fr.getStatus())
                .senderId(fr.getSender().getId())
                .senderName(fr.getSender().getName())
                .receiverId(fr.getReceiver().getId())
                .receiverName(fr.getReceiver().getName())
                .createdAt(fr.getCreatedAt())
                .build();
    }
}
