package com.habesha.community.repository;

import com.habesha.community.model.FriendRequest;
import com.habesha.community.model.FriendRequestStatus;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FriendRequest> findByReceiverAndStatusOrderByCreatedAtDesc(User receiver, FriendRequestStatus status);

    List<FriendRequest> findBySenderAndStatusOrderByCreatedAtDesc(User sender, FriendRequestStatus status);

    List<FriendRequest> findBySenderOrReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);

    // ✅ Counts incoming (receiver-side) pending requests for a given user id
    long countByReceiver_IdAndStatus(Long receiverId, FriendRequestStatus status);
}
