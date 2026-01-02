package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Rich user settings DTO exposed to the frontend Settings pages.
 * Keep fields nullable to allow partial updates and future expansion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    // Display
    private String theme;         // SYSTEM|LIGHT|DARK|HIGH_CONTRAST
    private String density;       // COMFORTABLE|COMPACT|SPACIOUS
    private String fontScale;     // SMALL|DEFAULT|LARGE or numeric-string if FE expects
    private Boolean reducedMotion;

    // Privacy / Profile visibility
    private String emailVisibility;  // PUBLIC|FRIENDS|REQUEST|ONLY_ME
    private String phoneVisibility;  // PUBLIC|FRIENDS|REQUEST|ONLY_ME
    private Boolean showOnlineStatus;
    private Boolean showLastSeen;
    private Boolean searchable;
    private String mentionsPolicy;   // EVERYONE|FRIENDS|NO_ONE
    private String dmPolicy;         // EVERYONE|FOAF|FRIENDS|NO_ONE

    // Notifications (opaque JSON as nested maps {category: {inApp, email, push}})
    private Map<String, Object> notifications;

    // Legacy fields for backward compat with older UIs
    private String language;
    private Boolean aiAssistEnabled;
    private Boolean notificationsEnabled;
}
