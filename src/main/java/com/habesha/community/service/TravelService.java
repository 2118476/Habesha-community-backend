package com.habesha.community.service;

import com.habesha.community.dto.TravelCreateRequest;
import com.habesha.community.dto.TravelPostResponse;
import com.habesha.community.model.Role;
import com.habesha.community.model.TravelPost;
import com.habesha.community.model.User;
import com.habesha.community.repository.TravelPostRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelPostRepository travelPostRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No current user"));
    }

    // --- Create ---

    @Transactional
    public TravelPostResponse create(TravelCreateRequest request) {
        User me = getCurrentUser();

        TravelPost post = TravelPost.builder()
                .user(me)
                .originCity(request.getOriginCity())
                .destinationCity(request.getDestinationCity())
                .travelDate(LocalDate.parse(request.getTravelDate())) // expects yyyy-MM-dd
                .message(request.getMessage())
                .contactMethod(request.getContactMethod())
                .build();

        TravelPost saved = travelPostRepository.save(post);
        return toResponse(saved);
    }

    // --- Read/List ---

    public List<TravelPostResponse> search(Optional<String> originOpt,
                                           Optional<String> destinationOpt,
                                           Optional<LocalDate> dateOpt) {
        String origin = originOpt.filter(s -> !s.isBlank()).orElse(null);
        String destination = destinationOpt.filter(s -> !s.isBlank()).orElse(null);
        LocalDate date = dateOpt.orElse(null);

        // Use existing repo query (exact match). If you want contains/ILIKE, we can update the repo.
        List<TravelPost> list = travelPostRepository.search(origin, destination, date);

        // Newest first
        list.sort(Comparator.comparing(TravelPost::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        return list.stream().map(this::toResponse).toList();
    }

    public TravelPostResponse getOne(Long id) {
        TravelPost post = travelPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel post not found"));
        return toResponse(post);
    }

    // --- Delete (only author or admin) ---

    @Transactional
    public void delete(Long id) {
        User me = getCurrentUser();
        TravelPost post = travelPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Travel post not found"));

        boolean isOwner = post.getUser() != null && post.getUser().getId().equals(me.getId());
        boolean isAdmin = me.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not authorised to delete this post");
        }
        travelPostRepository.delete(post);
    }

    // --- Mapper ---

    private TravelPostResponse toResponse(TravelPost p) {
        TravelPostResponse dto = new TravelPostResponse();
        dto.setId(p.getId());
        dto.setOriginCity(p.getOriginCity());
        dto.setDestinationCity(p.getDestinationCity());
        dto.setTravelDate(p.getTravelDate());
        dto.setMessage(p.getMessage());
        dto.setContactMethod(p.getContactMethod());
        dto.setCreatedAt(p.getCreatedAt());

        User u = p.getUser();
        if (u != null) {
            dto.setUserId(u.getId());
            dto.setUserUsername(u.getUsername());
            dto.setUserAvatar(u.getProfileImageUrl());

            String display = (u.getName() != null && !u.getName().isBlank())
                    ? u.getName()
                    : (u.getUsername() != null && !u.getUsername().isBlank())
                        ? u.getUsername()
                        : u.getEmail();
            dto.setUserName(display);
        }
        return dto;
    }
}
