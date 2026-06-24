package com.habesha.community.controller;

import com.habesha.community.dto.RegisterTokenRequest;
import com.habesha.community.model.DeviceToken;
import com.habesha.community.model.User;
import com.habesha.community.repository.DeviceTokenRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lets a signed-in client register (or remove) the device/browser push token
 * it received from Firebase, so the server can deliver message notifications.
 */
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushTokenController {

    private final DeviceTokenRepository tokenRepository;
    private final UserService userService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> register(@RequestBody RegisterTokenRequest body) {
        User me = userService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (body == null || body.getToken() == null || body.getToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Re-registering an existing token just reassigns it to the current user.
        DeviceToken dt = tokenRepository.findByToken(body.getToken()).orElseGet(DeviceToken::new);
        dt.setUser(me);
        dt.setToken(body.getToken());
        dt.setPlatform(body.getPlatform() != null ? body.getPlatform() : "ANDROID");
        tokenRepository.save(dt);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unregister")
    @Transactional
    public ResponseEntity<Void> unregister(@RequestBody RegisterTokenRequest body) {
        // Verifies the caller is authenticated, then drops the token if present.
        userService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (body != null && body.getToken() != null && !body.getToken().isBlank()) {
            tokenRepository.findByToken(body.getToken()).ifPresent(tokenRepository::delete);
        }
        return ResponseEntity.ok().build();
    }
}
