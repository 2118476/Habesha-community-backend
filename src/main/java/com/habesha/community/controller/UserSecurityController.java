package com.habesha.community.controller;

import com.habesha.community.dto.ChangePasswordRequest;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

/** Security endpoints for the current user. */
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSecurityController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Change password for the logged-in user. */
    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Optional<User> meOpt = userService.getCurrentUser();
        if (meOpt.isEmpty()) return ResponseEntity.status(401).build();
        User me = meOpt.get();

        if (!passwordEncoder.matches(req.getCurrentPassword(), me.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "INVALID_CURRENT_PASSWORD"));
        }
        if (req.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "WEAK_PASSWORD"));
        }
        me.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(me);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
