package com.habesha.community.controller;

import com.habesha.community.dto.ActivityItemDto;
import com.habesha.community.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller exposing the recent activity feed for the
 * authenticated user.  Clients can request a limited number of
 * items and optionally filter by an upper timestamp.
 */
@RestController
@RequiredArgsConstructor
public class ApiActivityController {

    private final ActivityService activityService;

    @GetMapping("/api/activity")
    public ResponseEntity<Map<String, List<ActivityItemDto>>> getActivity(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "before", required = false) String before
    ) {
        Instant beforeInstant = null;
        if (before != null && !before.isBlank()) {
            try {
                beforeInstant = Instant.parse(before);
            } catch (Exception ignored) {}
        }
        List<ActivityItemDto> items = activityService.getRecentActivity(limit, beforeInstant);
        Map<String, List<ActivityItemDto>> body = new HashMap<>();
        body.put("items", items);
        return ResponseEntity.ok(body);
    }
}