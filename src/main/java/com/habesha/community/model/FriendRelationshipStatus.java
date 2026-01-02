package com.habesha.community.model;

/**
 * Represents the high level relationship state between two users.
 * A friend request may be pending in either direction, accepted or
 * there may be no relationship at all.  Additional states such as
 * BLOCKED can be added in future iterations.
 */
public enum FriendRelationshipStatus {
    /** No friend request has ever been exchanged between the two users. */
    NONE,
    /** The current user has sent a friend request to the target and it is pending. */
    REQUEST_SENT,
    /** The current user has received a friend request from the target and it is pending. */
    REQUEST_RECEIVED,
    /** Both users have accepted the friendship and are friends. */
    FRIENDS,
    /** One user has blocked the other.  Not currently implemented. */
    BLOCKED
}