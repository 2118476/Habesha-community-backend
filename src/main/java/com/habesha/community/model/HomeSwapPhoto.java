package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"homeSwap", "path"})
@EqualsAndHashCode(of = "id")
@Entity
@Table(
    name = "home_swap_photo",
    indexes = {
        @Index(name = "idx_hsphoto_homeswap", columnList = "home_swap_id"),
        @Index(name = "idx_hsphoto_sort", columnList = "home_swap_id, sort_order")
    }
)
public class HomeSwapPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public URL consumed by the frontend (e.g. /uploads/homeswap/{postId}/{filename}) */
    @Column(nullable = false, length = 1024)
    private String url;

    /** Absolute filesystem path (server-side only; never expose) */
    @JsonIgnore
    @Column(nullable = false, length = 2048)
    private String path;

    @Column(length = 255)
    private String filename;

    @Column(length = 100)
    private String contentType;

    private Long sizeBytes; // use Long to allow null until set

    private Integer width;
    private Integer height;

    /**
     * Optional ordering within a post (0..n).
     * Keep it Integer (nullable) so we can assign later if needed.
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_swap_id", nullable = false)
    @JsonIgnore // prevent recursion; expose id via getter below
    private HomeSwap homeSwap;

    /** Expose the parent id without serializing the entire parent object */
    @JsonProperty("homeSwapId")
    @Transient
    public Long getHomeSwapId() {
        return homeSwap != null ? homeSwap.getId() : null;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // normalize sortOrder if missing
        // (often set in service when saving a batch; safe default here)
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}
