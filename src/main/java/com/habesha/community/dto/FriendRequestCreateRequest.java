package com.habesha.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for sending a friend request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestCreateRequest {

    @NotNull
    private Long receiverId;
}
