package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_block")
public class UserBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id")
    private User blocker;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id")
    private User blocked;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
