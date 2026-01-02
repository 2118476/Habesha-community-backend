package com.habesha.community.service;

import com.habesha.community.dto.UserProfileDto;
import com.habesha.community.dto.UserResponse;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.FriendRequestStatus;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.EventRepository;
import com.habesha.community.repository.FriendRequestRepository;
import com.habesha.community.repository.RentalRepository;
import com.habesha.community.repository.ServiceOfferRepository;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final RentalRepository rentalRepository;
    private final FriendRequestRepository friendRequestRepository;

    /** Resolve the signed-in user entity via Spring Security (email is the username). */
    public Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();
        Object principal = auth.getPrincipal();
        if (principal instanceof String s && "anonymousUser".equals(s)) return Optional.empty();
        String email = auth.getName();
        return userRepository.findByEmail(email);
    }

    // ---------- Profile badges ----------

    private void ensureBadgesCapacity(User user, int minSize) {
        if (user.getBadges() == null) {
            user.setBadges(new java.util.ArrayList<>());
        }
        List<String> badges = user.getBadges();
        while (badges.size() < minSize) {
            badges.add(null);
        }
        user.setBadges(badges);
    }

    @Transactional
    public UserProfileDto updateBadge(int index, String badgeId) {
        if (index < 0) throw new IllegalArgumentException("Badge index must be non-negative");
        User user = getCurrentUser().orElseThrow(() -> new IllegalStateException("Not authenticated"));
        ensureBadgesCapacity(user, index + 1);
        user.getBadges().set(index, badgeId);
        userRepository.save(user);
        return toProfile(user, true);
    }

    // ---------- DTO mappers ----------

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .city(user.getCity())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .frozen(user.getFrozen())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserSummaryDto toSummary(User user) {
        if (user == null) return null;
        String displayName = (user.getName() != null && !user.getName().isBlank())
                ? user.getName() : user.getUsername();
        Long friendsCount = null;
        Long postsCount = null; // not computed here
        try {
            friendsCount = friendRequestRepository != null
                    ? friendRequestRepository
                        .findBySenderOrReceiverAndStatus(user, user, FriendRequestStatus.ACCEPTED)
                        .stream().count()
                    : null;
        } catch (Exception ignored) { }
        return UserSummaryDto.builder()
                .id(user.getId())
                .displayName(displayName)
                .username(user.getUsername())
                .avatarUrl(user.getProfileImageUrl())
                .verified(false)
                .friendsCount(friendsCount)
                .postsCount(postsCount)
                .build();
    }

    public UserProfileDto toProfile(User user, boolean includeEmail) {
        if (user == null) return null;
        String displayName = (user.getName() != null && !user.getName().isBlank())
                ? user.getName() : user.getUsername();
        String joinDate = user.getCreatedAt() != null ? user.getCreatedAt().toString() : null;
        long eventsCount   = eventRepository   != null ? eventRepository.countByOrganizer_Id(user.getId()) : 0;
        long servicesCount = serviceOfferRepository != null ? serviceOfferRepository.countByProvider_Id(user.getId()) : 0;
        long rentalsCount  = rentalRepository  != null ? rentalRepository.countByOwner_Id(user.getId()) : 0;
        long friendsCount  = 0;
        if (friendRequestRepository != null) {
            try {
                friendsCount = friendRequestRepository
                        .findBySenderOrReceiverAndStatus(user, user, FriendRequestStatus.ACCEPTED)
                        .size();
            } catch (Exception ignored) { }
        }
        return UserProfileDto.builder()
                .id(user.getId())
                .displayName(displayName)
                .username(user.getUsername())
                .email(includeEmail ? user.getEmail() : null)
                .location(user.getCity())
                .bio(user.getBio())
                .avatarUrl(user.getProfileImageUrl())
                .bannerUrl(user.getBannerImageUrl())
                .joinDate(joinDate)
                .friendsCount(friendsCount)
                .eventsCount(eventsCount)
                .servicesCount(servicesCount)
                .rentalsCount(rentalsCount)
                .xp(user.getXp())
                .badges(user.getBadges())
                .twitter(user.getTwitter())
                .linkedin(user.getLinkedin())
                .instagram(user.getInstagram())
                .build();
    }

    // ---------- Basic CRUD / queries ----------

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public UserProfileDto updateCurrentUser(String displayName,
                                            String location,
                                            String avatarUrl,
                                            String bannerUrl,
                                            String bio,
                                            String twitter,
                                            String linkedin,
                                            String instagram) {
        User user = getCurrentUser().orElseThrow(() -> new IllegalStateException("Not authenticated"));
        if (displayName != null && !displayName.isBlank()) user.setName(displayName);
        if (location != null)                               user.setCity(location);
        if (avatarUrl != null && !avatarUrl.isBlank())      user.setProfileImageUrl(avatarUrl);
        if (bannerUrl != null && !bannerUrl.isBlank())      user.setBannerImageUrl(bannerUrl);
        if (bio != null)                                    user.setBio(bio);
        if (twitter != null)                                user.setTwitter(twitter);
        if (linkedin != null)                               user.setLinkedin(linkedin);
        if (instagram != null)                              user.setInstagram(instagram);
        userRepository.save(user);
        return toProfile(user, true);
    }

    public UserResponse getUserById(Long id) {
        return toResponse(getEntityById(id));
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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

    public List<User> findAllOrderedByXp() {
        return userRepository.findAll().stream()
                .sorted((u1, u2) -> Integer.compare(
                        u2.getXp() != null ? u2.getXp() : 0,
                        u1.getXp() != null ? u1.getXp() : 0))
                .collect(Collectors.toList());
    }

    // ---------- Avatar (DB stored) ----------

    /** Optional URL-based setter if you want to store a remote/data URL (kept, but renamed). */
    @Transactional
    public UserResponse updateProfileImageUrl(String url) {
        User u = getCurrentUser().orElseThrow(() -> new IllegalStateException("No current user"));
        u.setProfileImageUrl(url);
        userRepository.save(u);
        return toResponse(u);
    }

    /** Primary: store avatar bytes in DB and expose a stable local URL. */
    @Transactional
    public UserResponse updateProfileImage(byte[] bytes, String contentType) {
        User user = getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        user.setProfileImage(bytes);
        user.setProfileImageType(contentType != null ? contentType : "image/jpeg");
        user.setProfileImageUrl("/users/" + user.getId() + "/profile-image");
        userRepository.save(user);
        return toResponse(user);
    }

    // tiny DTO for streaming
    public static record UserImage(byte[] data, String contentType) {}

    public Optional<UserImage> getCurrentUserImage() {
        return getCurrentUser().map(u -> {
            if (u.getProfileImage() == null) return null;
            return new UserImage(u.getProfileImage(), u.getProfileImageType());
        });
    }

    public Optional<UserImage> getUserImageById(Long id) {
        return userRepository.findById(id).map(u -> {
            if (u.getProfileImage() == null) return null;
            return new UserImage(u.getProfileImage(), u.getProfileImageType());
        });
    }

    public void removeProfileImage() {
        User user = getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
        if (user.getProfileImage() != null || user.getProfileImageType() != null) {
            user.setProfileImage(null);
            user.setProfileImageType(null);
            user.setProfileImageUrl(null);
            userRepository.save(user);
        }
    }
    
    public User findById(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
}
