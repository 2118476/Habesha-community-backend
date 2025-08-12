package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Records details of a monetary transaction processed through Stripe.
 * Each payment links back to the user who paid and the entity
 * (service, event, ad, subscription) that the payment relates to.
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private User payer;

    private BigDecimal amount;

    private String currency;

    /**
     * Reference string returned by Stripe (e.g. session id, payment
     * intent id).  Useful for reconciling with Stripe dashboard.
     */
    private String reference;

    /**
     * Description of the purchase (e.g. "Service booking for Tax return help").
     */
    private String description;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    /**
     * ID of the entity this payment is associated with (serviceOfferId,
     * eventId, adId or subscription tier id).
     */
    private Long targetId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }
}