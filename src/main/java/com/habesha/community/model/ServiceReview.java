package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A review left by one user (reviewer) for a service provider.
 *
 * Anti-spam rule (enforced in the service layer): a reviewer may only review a
 * provider after a genuine two-way conversation — both parties must have
 * exchanged messages. One review per (provider, reviewer) pair; it can be edited.
 */
@Entity
@Table(
    name = "service_reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_service_review_provider_reviewer",
        columnNames = {"provider_id", "reviewer_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The service provider being reviewed. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    /** The user who wrote the review. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    /** 1–5 stars. */
    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
