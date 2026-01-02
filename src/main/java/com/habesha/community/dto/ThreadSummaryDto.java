package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A lightweight summary of a message thread between the current user and
 * another user. Includes the other user's id and display name, the
 * content and timestamp of the most recent message exchanged, a count of
 * unread messages, and the other user's avatar URL (for list rendering).
 *
 * Used by the inbox/dashboard to present a threads preview without
 * fetching the full conversation history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadSummaryDto {

    /** The id of the other user participating in the conversation. */
    private Long userId;

    /**
     * The display name of the other user. Typically their profile name
     * but may fall back to username or email depending on profile data.
     */
    private String userName;

    /**
     * Absolute or server-relative URL for the other user's profile
     * image. This enables the frontend sidebar to show avatars without
     * an extra API call. May be null if the user has no photo.
     *
     * Examples:
     *  - https://example.com/files/profile/42.jpg
     *  - /users/42/profile-image
     */
    private String avatarUrl;

    /** The content of the most recent message in the thread. */
    private String lastText;

    /** ISO-8601 instant of the most recent message timestamp. */
    private Instant lastAt;

    /**
     * Number of unread messages in this thread for the current user.
     * Only messages where the other user is the sender and the current
     * user is the recipient are counted.
     */
    private Long unread;
}
