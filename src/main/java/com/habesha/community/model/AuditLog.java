package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row per admin/moderator action — who did what, to whom, when and why.
 * Provides an accountability trail for the Trust &amp; Safety console.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who performed the action (admin/moderator user id + a display snapshot). */
    private Long actorId;
    private String actorName;

    /** Action key, e.g. REPORT_RESOLVED, CONTENT_REMOVED, USER_SUSPENDED, USER_WARNED. */
    @Column(nullable = false, length = 64)
    private String action;

    /** What was acted on, e.g. USER / RENTAL / SERVICE / REPORT. */
    @Column(length = 48)
    private String targetType;

    private Long targetId;

    /** Free-text detail (reason, notes). */
    @Column(length = 2000)
    private String detail;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
