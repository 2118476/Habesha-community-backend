// src/main/java/com/habesha/community/model/AdComment.java
package com.habesha.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ad_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which ad this comment belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id", nullable = false)
    @JsonIgnore
    private ClassifiedAd ad;

    // Author
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    // If this is a reply, who is the parent comment?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private AdComment parent;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* Convenience JSON fields for frontend */

    @JsonProperty("authorId")
    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }

    @JsonProperty("authorName")
    public String getAuthorName() {
        return author != null ? author.getName() : null;
    }

    @JsonProperty("authorAvatar")
    public Object getAuthorAvatar() {
        // You already started getAvatarUrl() on User.
        // If that's not ready yet, return null for now.
        return author != null ? author.getAvatarUrl() : null;
    }
}
