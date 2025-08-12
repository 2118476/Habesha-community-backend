package com.habesha.community.model;

/**
 * Enumeration of the different roles supported by the platform.
 *
 * <ul>
 *   <li>{@link #USER} – default role for end‑users who can browse listings,
 *       send friend requests, send messages and book services.</li>
 *   <li>{@link #SERVICE_PROVIDER} – users that offer services on the
 *       marketplace.  They can create service listings and respond
 *       to bookings.</li>
 *   <li>{@link #MODERATOR} – trusted users that can verify and moderate
 *       content such as events, classifieds and travel posts.</li>
 *   <li>{@link #ADMIN} – administrators with full control over the
 *       platform including user management and financial settings.</li>
 * </ul>
 */
public enum Role {
    USER,
    SERVICE_PROVIDER,
    MODERATOR,
    ADMIN
}