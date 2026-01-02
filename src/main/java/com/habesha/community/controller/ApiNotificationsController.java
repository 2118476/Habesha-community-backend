package com.habesha.community.controller;

import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class ApiNotificationsController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Mark notifications as "seen now" for the current user.
     * The UI calls this when opening the bell panel.
     */
    @PutMapping("/seen")
    public ResponseEntity<?> markSeen() {
        Optional<User> me = userService.getCurrentUser();
        if (me.isEmpty()) return ResponseEntity.status(401).build();

        User user = me.get();
        LocalDateTime now = LocalDateTime.now();
        user.setNotificationsSeenAt(now);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("ok", true, "seenAt", now.toString()));
    }
}
