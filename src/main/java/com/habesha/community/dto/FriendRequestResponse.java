// src/main/java/com/habesha/community/dto/FriendRequestResponse.java
package com.habesha.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.habesha.community.model.FriendRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendRequestResponse {
    private Long id;
    private FriendRequestStatus status;

    private Long senderId;
    private String senderName;
    private String senderUsername;
    private String senderAvatarUrl;

    private Long receiverId;
    private String receiverName;
    private String receiverUsername;
    private String receiverAvatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createdAt;
}
