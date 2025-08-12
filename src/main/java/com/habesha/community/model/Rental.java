package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A rental listing posted by a user.  Listings can represent
 * rooms, flats or shared spaces.  They can optionally be
 * featured (highlighted in search results) for a fee.  Images are
 * stored as a list of URL strings referencing external storage.
 */
@Entity
@Table(name = "rentals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private BigDecimal price;

    private String roomType;

    private String contact;

    @ElementCollection
    @CollectionTable(name = "rental_images", joinColumns = @JoinColumn(name = "rental_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    private boolean featured;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Expose owner info to the frontend
    @JsonProperty("ownerId")
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    @JsonProperty("ownerName")
    public String getOwnerName() {
        return owner != null ? owner.getName() : null;
    }
}