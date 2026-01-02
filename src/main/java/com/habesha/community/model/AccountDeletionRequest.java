package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_deletion_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountDeletionStatus status = AccountDeletionStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime handledAt;

    private Long handledByUserId;

    @Column(length = 1000)
    private String note;

    @Column(length = 500)
    private String requestedReason;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum AccountDeletionStatus {
        PENDING,
        APPROVED,
        REJECTED,
        COMPLETED
    }
}