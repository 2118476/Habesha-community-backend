package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountsResponse {
    private long unreadMessages;
    private long pendingRequests;
    private long notifications;
}
