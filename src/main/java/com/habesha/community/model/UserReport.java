package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A row each time someone reports a user.
 * Example: user A says user B is scamming.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_report")
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The person who made the report (the accuser) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    /** The person being reported (the accused) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "target_id")
    private User target;

    /** Free-text reason why they're being reported */
    @Column(length = 2000, nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private UserReportStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = UserReportStatus.OPEN;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
