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
 * Represents a service listed by a provider on the marketplace.
 * Services can be booked by end users. The platform charges a
 * commission on the base price (configured by admins). Providers
 * specify whether the service is delivered in person or online.
 */
@Entity
@Table(name = "service_offers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id")
    private User provider;

    private String category;

    private String title;

    @Column(length = 4000)
    private String description;

    private String estimatedTime;

    private BigDecimal basePrice;

    private String location;

    @Enumerated(EnumType.STRING)
    private ServiceMode mode;

    private boolean featured;

    private LocalDateTime createdAt;

    /** Cover image bytes, stored in the DB so they survive ephemeral disks. */
    @JsonIgnore
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.BINARY)
    @Column(name = "image_data", columnDefinition = "bytea")
    private byte[] imageData;

    @JsonIgnore
    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    /** True when this offer has a cover image stored. */
    @JsonProperty("hasImage")
    public boolean hasImage() {
        return imageData != null && imageData.length > 0;
    }

    /** Stable URL the frontend can use to render the cover image. */
    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return (id != null && hasImage()) ? "/api/services/" + id + "/image" : null;
    }

    // Expose provider info to the frontend
    @JsonProperty("providerId")
    public Long getProviderId() {
        return provider != null ? provider.getId() : null;
    }

    @JsonProperty("providerName")
    public String getProviderName() {
        return provider != null ? provider.getName() : null;
    }
}