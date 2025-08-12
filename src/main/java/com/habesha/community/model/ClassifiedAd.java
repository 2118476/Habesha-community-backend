package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Represents a classified advertisement posted by a user.  Classifieds
 * can cover a variety of categories such as phones, jobs, or other
 * goods/services.  They can be promoted (featured) for a fee.
 */
@Entity
@Table(name = "classified_ads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifiedAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "poster_id")
private User poster;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private BigDecimal price;

    private String imageUrl;

    private String category;

    private boolean featured;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Expose poster info to the frontend
    @JsonProperty("posterId")
    public Long getPosterId() {
        return poster != null ? poster.getId() : null;
    }

    @JsonProperty("posterName")
    public String getPosterName() {
        return poster != null ? poster.getName() : null;
    }
}