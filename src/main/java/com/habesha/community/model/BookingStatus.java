package com.habesha.community.model;

/**
 * Defines the various states a service booking can be in during its
 * lifecycle.  When a booking is requested it starts in the
 * {@link #PENDING} state.  Providers or admins can then confirm
 * (CONFIRMED) or cancel (CANCELLED) the booking.  Once the
 * service has been fulfilled the booking transitions to
 * {@link #COMPLETED}.
 */
public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}