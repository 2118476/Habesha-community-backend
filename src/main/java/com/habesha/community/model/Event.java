package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An event posted by a user.  Events can include weddings, meetups
 * and other community gatherings.  Admins can promote and verify
 * events to highlight them on the platform.
 */
@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private String title;

    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    private String location;

    private boolean featured;
    private boolean verified;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Expose organiser info
    @JsonProperty("organizerId")
    public Long getOrganizerId() {
        return organizer != null ? organizer.getId() : null;
    }

    @JsonProperty("organizerName")
    public String getOrganizerName() {
        return organizer != null ? organizer.getName() : null;
    }
}