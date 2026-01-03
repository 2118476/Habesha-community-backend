package com.habesha.community.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;
    private String city;

    // If you used Cloudinary before, keep the URL field; we can store a local URL here.
    private String profileImageUrl;

    private String password;

    @Column(length = 1024)
    private String bio;

    private String bannerImageUrl;

    @ElementCollection
    @CollectionTable(name = "user_badges", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "badge")
    @Builder.Default
    private List<String> badges = new java.util.ArrayList<>();

    @Column(name = "notifications_seen_at")
    private LocalDateTime notificationsSeenAt;

    @Builder.Default
    private Integer xp = 0;

    private String twitter;
    private String linkedin;
    private String instagram;

    private String resetPasswordToken;
    private String optionValue;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Column(name = "frozen")
    private Boolean frozen = false;

    @Column(name = "frozen_at")
    private LocalDateTime frozenAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private String language = "en";

    @Builder.Default
    @Column(name = "ai_assist_enabled")
    private Boolean aiAssistEnabled = false;

    @Builder.Default
    private Boolean notifications = true;

    /**
     * Timestamp of the user's most recent successful login.
     */
    private LocalDateTime lastLoginAt;

    /**
     * Timestamp of the user's most recent authenticated request.
     */
    private LocalDateTime lastActiveAt;

    // ===== Profile image blob (optional) =====
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_image")
    private byte[] profileImage;

    @Column(name = "profile_image_type", length = 100)
    private String profileImageType;

    // ===== JPA lifecycle =====
    @PrePersist
    protected void onCreate() {
        if (username == null || username.isBlank()) {
            username = deriveUsernameFromEmail(email);
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private static String deriveUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) return "user" + System.currentTimeMillis();
        return email.substring(0, email.indexOf('@'));
    }

    // ===== UserDetails =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // We authenticate with email as the username for Spring Security
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return active; }
    @Override public boolean isAccountNonLocked() { return active; }
    @Override public boolean isCredentialsNonExpired() { return active; }
    @Override public boolean isEnabled() { return active; }

    /**
     * Return a usable avatar URL for this user.
     * 1) If profileImageUrl exists (external/local), use it.
     * 2) Otherwise, fall back to /users/{id}/profile-image (served by controller).
     */
    public String getAvatarUrl() {
        if (this.profileImageUrl != null && !this.profileImageUrl.isBlank()) {
            return this.profileImageUrl;
        }
        if (this.id != null) {
            return "/users/" + this.id + "/profile-image";
        }
        return null;
    }

    // ===== Settings (display) =====
    private String theme;          // SYSTEM | LIGHT | DARK | HIGH_CONTRAST
    private String density;        // COMFORTABLE | COMPACT | SPACIOUS
    private String fontScale;      // SMALL | DEFAULT | LARGE
    private Boolean reducedMotion; // prefer reduced motion

    // ===== Settings (privacy) =====
    private String emailVisibility;  // PUBLIC | FRIENDS | REQUEST | ONLY_ME
    private String phoneVisibility;  // PUBLIC | FRIENDS | REQUEST | ONLY_ME
    private Boolean showOnlineStatus;
    private Boolean showLastSeen;
    private Boolean searchable;
    private String mentionsPolicy;   // EVERYONE | FRIENDS | NO_ONE
    private String dmPolicy;         // EVERYONE | FOAF | FRIENDS | NO_ONE

    // ===== Settings (notifications) =====
    @Lob
    private String notificationsJson; // JSON blob of notification categories
}
