// src/main/java/com/habesha/community/dto/RelationshipStatusResponse.java
package com.habesha.community.dto;

import com.habesha.community.model.FriendRelationshipStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RelationshipStatusResponse {
    private FriendRelationshipStatus status;
    /** If there is a pending request, we return its id and direction. */
    private Long pendingRequestId;  // nullable
    private Boolean iAmSender;      // nullable
}
