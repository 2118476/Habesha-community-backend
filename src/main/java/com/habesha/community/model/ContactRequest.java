package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "contact_request",
       uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "target_id", "type", "status"}))
public class ContactRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) private User requester;  // who asks
    @ManyToOne(optional = false) private User target;     // whose contact is requested

    @Enumerated(EnumType.STRING)
    private ContactType type; // EMAIL or PHONE

    @Enumerated(EnumType.STRING)
    private ContactRequestStatus status; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = ContactRequestStatus.PENDING;
    }
}
