package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a private message exchanged between two friends on the
 * platform.  Messages can also optionally be delivered via SMS
 * through Twilio when the recipient has a phone number configured
 * and the sender chooses to send via SMS.
 */
@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(length = 4000)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private boolean readByRecipient = false;

    private java.time.LocalDateTime readAt;

    /**
     * Indicates whether this message has been delivered via SMS.  When
     * true the Twilio service will have attempted to send the message
     * using the recipient's phone number.  Failed SMS attempts should
     * be logged separately for administrative troubleshooting.
     */
    @Builder.Default
    private boolean viaSms = false;

    private LocalDateTime sentAt;

    @PrePersist
    public void prePersist() {
        sentAt = LocalDateTime.now();
    }

    
    // Expose IDs to the front-end for bubble alignment
    @com.fasterxml.jackson.annotation.JsonProperty("senderId")
    public Long getSenderId() {
        return sender != null ? sender.getId() : null;
    }
    @com.fasterxml.jackson.annotation.JsonProperty("recipientId")
    public Long getRecipientId() {
        return recipient != null ? recipient.getId() : null;
    }
}