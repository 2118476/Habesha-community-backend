package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "photos"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(
    name = "home_swap",
    indexes = {
        @Index(name = "idx_homeswap_user", columnList = "user_id"),
        @Index(name = "idx_homeswap_created", columnList = "created_at")
    }
)
public class HomeSwap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String location;

    @Column(length = 4000)
    private String description;

    /* ---- Detail fields (all optional) ---- */
    @Column(name = "home_type", length = 40)
    private String homeType;

    private Integer bedrooms;
    private Integer bathrooms;

    @Column(name = "floor_level", length = 60)
    private String floorLevel;

    private Boolean parking;

    @Column(name = "garden_or_balcony")
    private Boolean gardenOrBalcony;

    @Column(name = "swap_window", length = 120)
    private String swapWindow;

    @Column(name = "preferred_location", length = 200)
    private String preferredLocation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "homeSwap", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<HomeSwapPhoto> photos = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Convenience helpers
    public void addPhoto(HomeSwapPhoto p) {
        photos.add(p);
        p.setHomeSwap(this);
    }

    public void removePhoto(HomeSwapPhoto p) {
        photos.remove(p);
        p.setHomeSwap(null);
    }
}
