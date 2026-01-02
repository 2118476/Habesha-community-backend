// src/main/java/com/habesha/community/service/FriendService.java
package com.habesha.community.service;

import com.habesha.community.dto.*;
import com.habesha.community.model.FriendRelationshipStatus;
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
    private final UserService userService;

    /* ==================== Core helpers ==================== */

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private Set<Long> friendIdsOf(User u) {
        return friendRequestRepository
                .findBySenderOrReceiverAndStatus(u, u, FriendRequestStatus.ACCEPTED)
                .stream()
                .map(fr -> fr.getSender().getId().equals(u.getId())
                        ? fr.getReceiver().getId()
                        : fr.getSender().getId())
                .collect(Collectors.toSet());
    }

    private UserSummaryDto summarize(User u) {
        return userService.toSummary(u);
    }

    private <T> PagedResponse<T> toPage(List<T> all, int page, int size) {
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        List<T> slice = all.subList(from, to);
        int totalPages = (int) Math.ceil(all.size() / (double) size);
        return new PagedResponse<>(slice, page, size, all.size(), totalPages, page + 1 >= totalPages);
    }

    /* ==================== Relationship ==================== */

    /**
     * Check if the current user and target user are friends.
     */
    public boolean areFriends(Long targetUserId) {
        User me = getCurrentUserOrThrow();
        if (me.getId().equals(targetUserId)) return false;

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<FriendRequest> direct = friendRequestRepository.findBySenderAndReceiver(me, target);
        if (direct.isPresent() && direct.get().getStatus() == FriendRequestStatus.ACCEPTED) {
            return true;
        }
        
        Optional<FriendRequest> reverse = friendRequestRepository.findBySenderAndReceiver(target, me);
        return reverse.isPresent() && reverse.get().getStatus() == FriendRequestStatus.ACCEPTED;
    }

    public FriendRelationshipStatus getRelationshipStatus(Long targetUserId) {
        User me = getCurrentUserOrThrow();
        if (me.getId().equals(targetUserId)) return FriendRelationshipStatus.FRIENDS;

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<FriendRequest> direct = friendRequestRepository.findBySenderAndReceiver(me, target);
        if (direct.isPresent()) {
            var fr = direct.get();
            if (fr.getStatus() == FriendRequestStatus.PENDING) return FriendRelationshipStatus.REQUEST_SENT;
            if (fr.getStatus() == FriendRequestStatus.ACCEPTED) return FriendRelationshipStatus.FRIENDS;
        }
        Optional<FriendRequest> reverse = friendRequestRepository.findBySenderAndReceiver(target, me);
        if (reverse.isPresent()) {
            var fr = reverse.get();
            if (fr.getStatus() == FriendRequestStatus.PENDING) return FriendRelationshipStatus.REQUEST_RECEIVED;
            if (fr.getStatus() == FriendRequestStatus.ACCEPTED) return FriendRelationshipStatus.FRIENDS;
        }
        return FriendRelationshipStatus.NONE;
    }

    public RelationshipStatusResponse getRelationshipStatusResponse(Long targetUserId) {
        User me = getCurrentUserOrThrow();
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<FriendRequest> direct = friendRequestRepository.findBySenderAndReceiver(me, target);
        if (direct.isPresent()) {
            var fr = direct.get();
            if (fr.getStatus() == FriendRequestStatus.PENDING) {
                return RelationshipStatusResponse.builder()
                        .status(FriendRelationshipStatus.REQUEST_SENT)
                        .pendingRequestId(fr.getId())
                        .iAmSender(true)
                        .build();
            }
            if (fr.getStatus() == FriendRequestStatus.ACCEPTED) {
                return RelationshipStatusResponse.builder()
                        .status(FriendRelationshipStatus.FRIENDS)
                        .build();
            }
        }
        Optional<FriendRequest> reverse = friendRequestRepository.findBySenderAndReceiver(target, me);
        if (reverse.isPresent()) {
            var fr = reverse.get();
            if (fr.getStatus() == FriendRequestStatus.PENDING) {
                return RelationshipStatusResponse.builder()
                        .status(FriendRelationshipStatus.REQUEST_RECEIVED)
                        .pendingRequestId(fr.getId())
                        .iAmSender(false)
                        .build();
            }
            if (fr.getStatus() == FriendRequestStatus.ACCEPTED) {
                return RelationshipStatusResponse.builder()
                        .status(FriendRelationshipStatus.FRIENDS)
                        .build();
            }
        }
        return RelationshipStatusResponse.builder()
                .status(FriendRelationshipStatus.NONE)
                .build();
    }

    @Transactional
    public void removeFriend(Long targetUserId) {
        User me = getCurrentUserOrThrow();
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<FriendRequest> direct = friendRequestRepository.findBySenderAndReceiver(me, target);
        if (direct.isPresent() && direct.get().getStatus() == FriendRequestStatus.ACCEPTED) {
            friendRequestRepository.delete(direct.get());
            return;
        }
        Optional<FriendRequest> reverse = friendRequestRepository.findBySenderAndReceiver(target, me);
        if (reverse.isPresent() && reverse.get().getStatus() == FriendRequestStatus.ACCEPTED) {
            friendRequestRepository.delete(reverse.get());
        }
    }

    /* ==================== Requests ==================== */

    @Transactional
    public void sendRequest(FriendRequestCreateRequest request) {
        User sender = getCurrentUserOrThrow();
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot send a friend request to yourself");
        }

        boolean exists = friendRequestRepository.findBySenderAndReceiver(sender, receiver).isPresent()
                || friendRequestRepository.findBySenderAndReceiver(receiver, sender).isPresent();
        if (exists) throw new IllegalStateException("A friend request already exists");

        FriendRequest entity = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(entity);
    }

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

    @Transactional
    public void cancelOutgoingRequest(Long requestId) {
        User me = getCurrentUserOrThrow();
        FriendRequest req = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));
        if (!req.getSender().getId().equals(me.getId())) {
            throw new IllegalStateException("You are not the sender of this request");
        }
        if (req.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be cancelled");
        }
        friendRequestRepository.delete(req);
    }

    /* ==================== Queries & paging ==================== */

    public PagedResponse<UserSummaryDto> getFriendsPage(int page, int size) {
        User me = getCurrentUserOrThrow();
        Set<Long> ids = friendIdsOf(me);
        List<UserSummaryDto> all = ids.stream()
                .map(id -> summarize(userService.getEntityById(id)))
                .sorted(Comparator.comparing(UserSummaryDto::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return toPage(all, page, size);
    }

    public List<UserSummaryDto> getMutualFriends(Long targetUserId) {
        User me = getCurrentUserOrThrow();
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<Long> myFriends = friendIdsOf(me);
        Set<Long> theirFriends = friendIdsOf(target);

        List<UserSummaryDto> mutuals = myFriends.stream()
                .filter(theirFriends::contains)
                .map(id -> summarize(userService.getEntityById(id)))
                .sorted(Comparator.comparing(UserSummaryDto::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // populate mutualCount= number of mutuals with me (for each item, it's all mutuals except itself)
        for (UserSummaryDto dto : mutuals) {
            dto.setMutualCount(mutuals.size() - 1); // small UX hint; tweak if you prefer exact per-user counts elsewhere
        }
        return mutuals;
    }

    public PagedResponse<FriendRequestResponse> getIncomingRequestsPage(int page, int size) {
        List<FriendRequestResponse> all = getIncomingRequests();
        return toPage(all, page, size);
    }

    public PagedResponse<FriendRequestResponse> getOutgoingRequestsPage(int page, int size) {
        List<FriendRequestResponse> all = getOutgoingRequests();
        return toPage(all, page, size);
    }

    public List<FriendRequestResponse> getIncomingRequests() {
        User current = getCurrentUserOrThrow();
        return friendRequestRepository
                .findByReceiverAndStatusOrderByCreatedAtDesc(current, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FriendRequestResponse> getOutgoingRequests() {
        User current = getCurrentUserOrThrow();
        return friendRequestRepository
                .findBySenderAndStatusOrderByCreatedAtDesc(current, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    FriendRequestResponse toResponse(FriendRequest fr) {
        return FriendRequestResponse.builder()
                .id(fr.getId())
                .status(fr.getStatus())
                .senderId(fr.getSender().getId())
                .senderName(fr.getSender().getName())
                .senderUsername(fr.getSender().getUsername())
                .senderAvatarUrl(fr.getSender().getProfileImageUrl())
                .receiverId(fr.getReceiver().getId())
                .receiverName(fr.getReceiver().getName())
                .receiverUsername(fr.getReceiver().getUsername())
                .receiverAvatarUrl(fr.getReceiver().getProfileImageUrl())
                .createdAt(fr.getCreatedAt())
                .build();
    }

    /* ==================== Discovery ==================== */

    public List<UserSummaryDto> getFriendSuggestions(int limit) {
        User me = getCurrentUserOrThrow();

        // exclude set: me + friends + pending in/out
        Set<Long> exclude = new HashSet<>();
        exclude.add(me.getId());
        friendIdsOf(me).forEach(exclude::add);
        getIncomingRequests().forEach(r -> exclude.add(r.getSenderId()));
        getOutgoingRequests().forEach(r -> exclude.add(r.getReceiverId()));

        Set<Long> myFriendIds = friendIdsOf(me);

        List<UserSummaryDto> ranked = userRepository.findAll().stream()
                .filter(u -> !exclude.contains(u.getId()))
                .map(u -> {
                    // mutuals with me
                    int mutual = 0;
                    for (Long id : friendIdsOf(u)) if (myFriendIds.contains(id)) mutual++;
                    UserSummaryDto dto = summarize(u);
                    dto.setMutualCount(mutual);
                    return dto;
                })
                .sorted(Comparator
                        .comparing(UserSummaryDto::getMutualCount, Comparator.nullsFirst(Integer::compareTo)).reversed()
                        .thenComparing(UserSummaryDto::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());

        return ranked;
    }

    public PagedResponse<UserSummaryDto> searchUsers(String query, int page, int size) {
        if (query == null || query.trim().length() < 2) {
            return toPage(List.of(), page, size);
        }
        List<User> matches = userRepository
                .findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);

        // Exclude myself
        Long meId = getCurrentUserOrThrow().getId();
        List<UserSummaryDto> all = matches.stream()
                .filter(u -> !Objects.equals(u.getId(), meId))
                .map(this::summarize)
                .sorted(Comparator.comparing(UserSummaryDto::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        return toPage(all, page, size);
    }
}
