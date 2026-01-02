package com.habesha.community.dto;

import lombok.Data;
import java.util.Map;

/**
 * Update payload for user settings.
 * All fields optional; unspecified values remain unchanged.
 */
@Data
public class UserSettingsUpdateRequest {
    // Display
    private String theme;
    private String density;
    private String fontScale;
    private Boolean reducedMotion;

    // Privacy
    private String emailVisibility;
    private String phoneVisibility;
    private Boolean showOnlineStatus;
    private Boolean showLastSeen;
    private Boolean searchable;
    private String mentionsPolicy;
    private String dmPolicy;

    // Notifications
    private Map<String, Object> notifications;

    // Legacy
    private String language;
    private Boolean aiAssistEnabled;
    private Boolean notificationsEnabled;
}
