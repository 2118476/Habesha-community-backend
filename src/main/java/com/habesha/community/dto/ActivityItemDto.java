package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityItemDto {
    private String id;          // synthetic id: type:id
    private String type;        // EVENT_CREATED | SERVICE_CREATED | TRAVEL_POSTED | FRIEND_ACCEPTED | MESSAGE_RECEIVED
    private UserSummaryDto actor;
    private String entityType;  // event|service|travel|rental|message
    private Long entityId;
    private String title;       // optional title/preview
    private Instant createdAt;
}
