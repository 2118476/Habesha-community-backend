package com.habesha.community.dto;

import com.habesha.community.model.FriendRequestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO representing a friend request, either incoming or outgoing.
 */
@Data
@Builder
public class FriendRequestResponse {
    private Long id;
    private FriendRequestStatus status;

    private Long senderId;
    private String senderName;

    private Long receiverId;
    private String receiverName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
