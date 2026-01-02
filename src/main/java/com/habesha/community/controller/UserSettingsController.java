package com.habesha.community.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.habesha.community.dto.UserSettingsDto;
import com.habesha.community.dto.UserSettingsUpdateRequest;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User me() {
        // In this codebase, SecurityContext stores com.habesha.community.model.User as principal
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Object p = auth != null ? auth.getPrincipal() : null;
        if (p instanceof User u) return u;
        if (p instanceof org.springframework.security.core.userdetails.User du) {
            return userRepository.findByUsername(du.getUsername()).orElseThrow();
        }
        throw new IllegalStateException("Unauthenticated");
    }

    @GetMapping
    public ResponseEntity<UserSettingsDto> get() {
        var u = userRepository.findById(me().getId()).orElseThrow();
        Map<String,Object> notifications = parseJson(u.getNotificationsJson());
        var dto = UserSettingsDto.builder()
                .theme(nullToDefault(u.getTheme(), "SYSTEM"))
                .density(nullToDefault(u.getDensity(), "COMFORTABLE"))
                .fontScale(nullToDefault(u.getFontScale(), "DEFAULT"))
                .reducedMotion(boolOr(u.getReducedMotion(), false))
                .emailVisibility(nullToDefault(u.getEmailVisibility(), "FRIENDS"))
                .phoneVisibility(nullToDefault(u.getPhoneVisibility(), "REQUEST"))
                .showOnlineStatus(boolOr(u.getShowOnlineStatus(), true))
                .showLastSeen(boolOr(u.getShowLastSeen(), true))
                .searchable(boolOr(u.getSearchable(), true))
                .mentionsPolicy(nullToDefault(u.getMentionsPolicy(), "EVERYONE"))
                .dmPolicy(nullToDefault(u.getDmPolicy(), "FRIENDS"))
                .notifications(notifications)
                .language(u.getLanguage())
                .aiAssistEnabled(u.getAiAssistEnabled())
                .notificationsEnabled(u.getNotifications())
                .build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<UserSettingsDto> update(@RequestBody UserSettingsUpdateRequest r) {
        var u = userRepository.findById(me().getId()).orElseThrow();

        if (r.getTheme() != null) u.setTheme(r.getTheme());
        if (r.getDensity() != null) u.setDensity(r.getDensity());
        if (r.getFontScale() != null) u.setFontScale(r.getFontScale());
        if (r.getReducedMotion() != null) u.setReducedMotion(r.getReducedMotion());

        if (r.getEmailVisibility() != null) u.setEmailVisibility(r.getEmailVisibility());
        if (r.getPhoneVisibility() != null) u.setPhoneVisibility(r.getPhoneVisibility());
        if (r.getShowOnlineStatus() != null) u.setShowOnlineStatus(r.getShowOnlineStatus());
        if (r.getShowLastSeen() != null) u.setShowLastSeen(r.getShowLastSeen());
        if (r.getSearchable() != null) u.setSearchable(r.getSearchable());
        if (r.getMentionsPolicy() != null) u.setMentionsPolicy(r.getMentionsPolicy());
        if (r.getDmPolicy() != null) u.setDmPolicy(r.getDmPolicy());

        if (r.getNotifications() != null) {
            u.setNotificationsJson(toJson(r.getNotifications()));
        }

        if (r.getLanguage() != null) u.setLanguage(r.getLanguage());
        if (r.getAiAssistEnabled() != null) u.setAiAssistEnabled(r.getAiAssistEnabled());
        if (r.getNotificationsEnabled() != null) u.setNotifications(r.getNotificationsEnabled());

        userRepository.save(u);
        return get();
    }

    private Map<String,Object> parseJson(String json) {
        if (json == null || json.isBlank()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    private String toJson(Map<String,Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
    private String nullToDefault(String v, String def) { return (v == null || v.isBlank()) ? def : v; }
    private Boolean boolOr(Boolean v, boolean d) { return v != null ? v : d; }
}
