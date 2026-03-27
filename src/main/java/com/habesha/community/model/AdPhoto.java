package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "ad_photo")
public class AdPhoto {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stored absolute or relative filesystem path to the image
    @Column(nullable = false, length = 512)
    private String filePath;

    // Optional: original filename
    @Column(nullable = false, length = 255)
    private String filename;

    // Optional: small caption/alt text
    @Column(length = 500)
    private String caption;

    // For ordering
    @Column(nullable = false)
    private Integer sortIndex;

    @Lob
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.BINARY)
    @Column(name = "image_data", columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_id", nullable = false)
    @JsonIgnore
    private ClassifiedAd ad;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (sortIndex == null) sortIndex = 0;
    }
}