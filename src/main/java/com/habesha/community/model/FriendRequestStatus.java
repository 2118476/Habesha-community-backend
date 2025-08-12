package com.habesha.community.model;

/**
 * Status values for a friendship request between two users.  Requests
 * progress from {@link #PENDING} to either {@link #ACCEPTED} or
 * {@link #REJECTED}.  Once accepted, users become friends and can
 * exchange messages in the internal chat system.
 */
public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}