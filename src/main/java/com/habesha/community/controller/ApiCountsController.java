package com.habesha.community.controller;

import com.habesha.community.model.User;
import com.habesha.community.service.CountsService;
import com.habesha.community.service.NotificationCounterService;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiCountsController {

    private final CountsService countsService;
    private final UserService userService;
    private final NotificationCounterService notificationCounterService;

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> getCounts() {
        long unreadMessages   = countsService.getUnreadMessagesTotal();
        long pendingRequests  = countsService.getPendingFriendRequests();
        long notifications    = unreadMessages + pendingRequests;

        // For unreadNotifications we add new "social" interactions since last seen
        Optional<User> me = userService.getCurrentUser();
        Long myId = me.map(User::getId).orElse(null);
        LocalDateTime since = me.map(User::getNotificationsSeenAt)
                                // first-time users: treat as now (0 new), the panel still shows full history
                                .orElse(LocalDateTime.now());

        long newSocial = (myId != null)
                ? notificationCounterService.countNewAdInteractionsSince(myId, since)
                : 0L;

        long unreadNotifications = notifications + newSocial;

        Map<String, Long> response = new HashMap<>();
        response.put("unreadMessages", unreadMessages);
        response.put("pendingRequests", pendingRequests);
        response.put("notifications", notifications);
        response.put("unreadNotifications", unreadNotifications);
        return ResponseEntity.ok(response);
    }
}
