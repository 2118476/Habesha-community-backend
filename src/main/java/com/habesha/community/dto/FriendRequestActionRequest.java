package com.habesha.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for accepting or rejecting a friend request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestActionRequest {

    @NotNull
    private Long requestId;

    /**
     * true = accept, false = reject
     */
    private boolean accept;
}
