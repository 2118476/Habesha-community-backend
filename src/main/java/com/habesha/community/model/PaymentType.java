package com.habesha.community.model;

/**
 * Describes what a payment relates to.  Payments can be for service
 * bookings, event promotions, classified ads promotions or
 * subscriptions to premium tiers.  This enumeration allows the
 * {@link com.habesha.community.model.Payment} entity to record the
 * context of the transaction.
 */
public enum PaymentType {
    SERVICE,
    EVENT,
    AD,
    SUBSCRIPTION
}