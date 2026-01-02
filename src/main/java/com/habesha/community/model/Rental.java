package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A rental listing posted by a user.
 * Images:
 *  - Legacy external URLs: images (ElementCollection)
 *  - New local storage: photos (RentalPhoto entities)
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

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String location;

    private BigDecimal price;

    @Column(length = 50)
    private String roomType;

    @Column(length = 100)
    private String contact;

    /** Legacy list of external image URLs. Keep for backward compatibility. */
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "rental_images", joinColumns = @JoinColumn(name = "rental_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    private boolean featured;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** New photos stored on server filesystem (preferred moving forward). */
    @Builder.Default
    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortIndex ASC, id ASC")
    @JsonManagedReference
    private List<RentalPhoto> photos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Convenience helpers
    public void addPhoto(RentalPhoto p) {
        photos.add(p);
        p.setRental(this);
    }
    public void removePhoto(RentalPhoto p) {
        photos.remove(p);
        p.setRental(null);
    }

    // Expose owner info to the frontend without serializing owner object
    @JsonProperty("ownerId")
    public Long getOwnerId() {
        return owner != null ? owner.getId() : null;
    }
    @JsonProperty("ownerName")
    public String getOwnerName() {
        return owner != null ? owner.getName() : null;
    }

    /*
     * IMPORTANT:
     * Removed the old getCity() method that threw UnsupportedOperationException,
     * which caused Jackson to throw 500s during serialization.
     *
     * If you later add a real 'city' column, define it as a proper field with getter/setter.
     */
}
