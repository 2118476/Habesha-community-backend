package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing various counter values used in the navigation bar.
 *
 * <p>
 * This object contains the number of unread messages for the current user,
 * the number of pending friend requests awaiting action, and a general
 * notifications count. For now the notifications value is simply the sum
 * of unread messages and pending requests, but this can be expanded in
 * future revisions to include other notification types (e.g. alerts,
 * system messages) without changing the API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountsDto {
    /** number of unread direct messages across all threads */
    private long unreadMessages;
    /** number of incoming friend requests with status PENDING */
    private long pendingRequests;
    /** aggregated notification count; currently unreadMessages + pendingRequests */
    private long notifications;
}