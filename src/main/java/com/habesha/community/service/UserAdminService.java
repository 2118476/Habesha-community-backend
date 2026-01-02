package com.habesha.community.service;

import com.habesha.community.dto.AdminUserDto;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Service for administrative user management. Provides paginated
 * retrieval of users and allows updating user roles. All methods
 * should be secured at the controller level.
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;

    /**
     * Returns a page of users with optional case‑insensitive
     * search on name or username. When a query is provided, a
     * best‑effort manual paging is done over the full list of
     * matches. If no query is supplied, JPA paging is used directly.
     *
     * @param query optional search string to filter by name/username
     * @param pageable the paging information
     * @return a page of AdminUserDto objects
     */
    public Page<AdminUserDto> listUsers(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            List<User> all = userRepository
                    .findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(query, query);
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), all.size());
            List<AdminUserDto> slice = all.subList(Math.min(start, all.size()), end)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return new PageImpl<>(slice, pageable, all.size());
        } else {
            Page<User> page = userRepository.findAll(pageable);
            return page.map(this::toDto);
        }
    }

    /**
     * Update a user's role. This method performs a simple role
     * assignment and persists it. The caller is responsible for
     * enforcing authorization.
     *
     * @param userId the id of the user whose role to change
     * @param newRole the new role to assign
     */
    @Transactional
    public void updateRole(Long userId, Role newRole) {
        Objects.requireNonNull(newRole, "newRole must not be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(newRole);
        userRepository.save(user);
    }

    private AdminUserDto toDto(User u) {
        // Determine whether the user is considered online.  For the
        // purposes of the admin dashboard a user is online if they
        // have made an authenticated request within the past 5 minutes.
        boolean isOnline = false;
        LocalDateTime lastActive = u.getLastActiveAt();
        if (lastActive != null) {
            // Compare against now minus 5 minutes to determine
            // recent activity.  A negative result means lastActive is
            // after the threshold (i.e. within the last 5 minutes).
            isOnline = lastActive.isAfter(LocalDateTime.now().minusMinutes(5));
        }
        return AdminUserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .active(u.isActive())
                .lastLoginAt(u.getLastLoginAt())
                .lastActiveAt(u.getLastActiveAt())
                .online(isOnline)
                .build();
    }

    /**
     * Retrieve a single user by id and convert it to an AdminUserDto.  If
     * the user does not exist an IllegalArgumentException is thrown.
     *
     * @param id the id of the user to fetch
     * @return a populated AdminUserDto
     */
    public AdminUserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return toDto(user);
    }

    /**
     * Permanently delete a user from the system.  Be cautious when
     * calling this – removing a user may orphan related records.
     *
     * @param id the id of the user to remove
     */
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Set the active flag on a user.  When active is false the
     * account is disabled for login and API access.  When true the
     * account is enabled.  If the user does not exist an exception
     * is thrown.
     *
     * @param id    the id of the user
     * @param value true to activate, false to deactivate
     */
    @Transactional
    public void setActive(Long id, boolean value) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setActive(value);
        userRepository.save(user);
    }
}