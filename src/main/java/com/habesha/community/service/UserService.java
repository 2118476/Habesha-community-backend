package com.habesha.community.service;

import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Provides CRUD operations for users. Includes helpers for
 * retrieving the currently authenticated user and for listing users
 * by role. The admin service should use this for managing user
 * roles and deactivation.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        String email = auth.getName(); // This is the username/email used for login
        return userRepository.findByEmail(email);
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername()) // ✅ added
                .email(user.getEmail())
                .phone(user.getPhone())
                .city(user.getCity())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .build();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    /**
     * Update the current user's profile image URL.  The profile image
     * is provided as a data URL or remote URL.  After updating
     * the entity, the updated UserResponse is returned.
     */
    @Transactional
    public UserResponse updateProfileImage(String url) {
        var opt = getCurrentUser();
        if (opt.isEmpty()) {
            throw new IllegalStateException("No current user");
        }
        var u = opt.get();
        u.setProfileImageUrl(url);
        userRepository.save(u);
        return toResponse(u);
    }
}
