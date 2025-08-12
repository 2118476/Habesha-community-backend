package com.habesha.community.model;

/**
 * Status of a payment record.  Payments start as {@link #PENDING}
 * when the checkout session is created.  Upon successful Stripe
 * webhook confirmation they transition to {@link #SUCCEEDED}.  Failed
 * or cancelled payments move to {@link #FAILED}.
 */
public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED
}