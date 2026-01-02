package com.habesha.community.service;

import com.habesha.community.dto.HomeSwapRequest;
import com.habesha.community.dto.HomeSwapResponse;
import com.habesha.community.model.HomeSwap;
import com.habesha.community.model.HomeSwapPhoto;
import com.habesha.community.model.User;
import com.habesha.community.repository.HomeSwapPhotoRepository;
import com.habesha.community.repository.HomeSwapRepository;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeSwapService {

    private static final int MAX_PHOTOS = 6;

    private final HomeSwapRepository repo;
    private final HomeSwapPhotoRepository photoRepo;
    private final UserRepository userRepository;
    private final DiskStorageService storage;

    /* ---------- helpers ---------- */

    private Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private User me() {
        Authentication a = auth();
        if (a == null || a.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return userRepository.findByEmail(a.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAdmin(Authentication a) {
        if (a == null) return false;
        for (GrantedAuthority ga : a.getAuthorities()) {
            String role = ga.getAuthority();
            if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    /**
     * Determine if the current authentication holds a moderator role.  The roles
     * may be prefixed with ROLE_ depending on how Spring Security is
     * configured.  This helper mirrors the admin check logic but for
     * moderators.  Note that moderators are distinct from admins and do not
     * imply elevated permissions beyond those explicitly granted in service
     * methods.
     */
    private boolean isModerator(Authentication a) {
        if (a == null) return false;
        for (GrantedAuthority ga : a.getAuthorities()) {
            String role = ga.getAuthority();
            if ("MODERATOR".equalsIgnoreCase(role) || "ROLE_MODERATOR".equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    private RuntimeException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    /* ---------- create (JSON only) ---------- */

    @Transactional
    public HomeSwapResponse create(HomeSwapRequest req) {
        if (req == null) throw bad("Payload is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw bad("Title is required");
        if (req.getLocation() == null || req.getLocation().isBlank()) throw bad("Location is required");

        User u = me();
        HomeSwap e = HomeSwap.builder()
                .user(u)
                .title(req.getTitle().trim())
                .location(req.getLocation().trim())
                .description(req.getDescription() == null ? null : req.getDescription().trim())
                .build();

        // make sure photos list is usable
        if (e.getPhotos() == null) e.setPhotos(new ArrayList<>());

        e = repo.save(e);
        return toRes(e);
    }

    /* ---------- create (multipart with photos) ---------- */

    @Transactional
    public HomeSwapResponse create(HomeSwapRequest req, List<MultipartFile> photos) {
        if (req == null) throw bad("Payload is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw bad("Title is required");
        if (req.getLocation() == null || req.getLocation().isBlank()) throw bad("Location is required");

        // Only enforce count here; DiskStorageService will enforce size/MIME/etc.
        int count = (photos == null) ? 0 : (int) photos.stream().filter(f -> f != null && !f.isEmpty()).count();
        if (count > MAX_PHOTOS) throw bad("Max " + MAX_PHOTOS + " photos");

        User u = me();
        HomeSwap e = HomeSwap.builder()
                .user(u)
                .title(req.getTitle().trim())
                .location(req.getLocation().trim())
                .description(req.getDescription() == null ? null : req.getDescription().trim())
                .build();

        if (e.getPhotos() == null) e.setPhotos(new ArrayList<>());

        e = repo.save(e); // get ID

        int sort = 0;
        if (photos != null) {
            for (MultipartFile file : photos) {
                if (file == null || file.isEmpty()) continue;
                try {
                    DiskStorageService.StoredImage s = storage.storeForHomeSwap(e.getId(), file);
                    HomeSwapPhoto p = HomeSwapPhoto.builder()
                            .homeSwap(e)
                            .url(s.getUrl())          // public URL for <img src=...>
                            .path(s.getPath())        // absolute FS path (for delete)
                            .filename(s.getFilename())
                            .contentType(s.getContentType())
                            .sizeBytes(s.getSizeBytes())
                            .width(s.getWidth())
                            .height(s.getHeight())
                            .sortOrder(sort++)
                            .createdAt(s.getSavedAt())
                            .build();
                    photoRepo.save(p);
                    e.getPhotos().add(p);
                } catch (Exception ex) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Failed to store image: " + safeName(file),
                            ex
                    );
                }
            }
        }
        return toRes(e);
    }

    /* ---------- read ---------- */

    @Transactional(readOnly = true)
    public List<HomeSwapResponse> list() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(
                        HomeSwap::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HomeSwapResponse getOne(Long id) {
        return toRes(repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found")));
    }

    /* ---------- update ---------- */

    @Transactional
    public HomeSwapResponse update(Long id, HomeSwapRequest req) {
        HomeSwap e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User u = me();
        Authentication a = auth();
        boolean owner = e.getUser() != null && e.getUser().getId() != null && e.getUser().getId().equals(u.getId());
        // Only the owner may update a HomeSwap.  Administrators and moderators
        // may not edit other users' posts via this API.
        if (!owner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised to update");
        }

        if (req == null) throw bad("Payload is required");
        if (req.getTitle() == null || req.getTitle().isBlank()) throw bad("Title is required");
        if (req.getLocation() == null || req.getLocation().isBlank()) throw bad("Location is required");

        e.setTitle(req.getTitle().trim());
        e.setLocation(req.getLocation().trim());
        e.setDescription(req.getDescription() == null ? null : req.getDescription().trim());

        e = repo.save(e);
        return toRes(e);
    }

    /* ---------- delete ---------- */

    @Transactional
    public void delete(Long id) {
        HomeSwap e = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        User u = me();
        Authentication a = auth();
        boolean owner = e.getUser() != null && e.getUser().getId() != null && e.getUser().getId().equals(u.getId());
        boolean admin = isAdmin(a);
        boolean moderator = isModerator(a);
        // Permit deletion by the owner or by administrators/moderators.  This
        // matches the Rentals behaviour which allows admins to delete any
        // listing, and extends it to moderators for HomeSwap posts.
        if (!owner && !admin && !moderator) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised to delete");
        }

        if (e.getPhotos() != null) {
            e.getPhotos().forEach(p -> {
                try { storage.deletePath(p.getPath()); } catch (Exception ignore) {}
            });
        }
        repo.delete(e);
    }

    /* ---------- mapping ---------- */

    private HomeSwapResponse toRes(HomeSwap e) {
        HomeSwapResponse dto = new HomeSwapResponse();
        dto.setId(e.getId());
        dto.setTitle(e.getTitle());
        dto.setLocation(e.getLocation());
        dto.setDescription(e.getDescription());
        dto.setCreatedAt(e.getCreatedAt());

        User user = e.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserUsername(user.getUsername());
            dto.setUserAvatar(user.getProfileImageUrl());
            String display = (user.getName() != null && !user.getName().isBlank())
                    ? user.getName()
                    : (user.getUsername() != null && !user.getUsername().isBlank())
                        ? user.getUsername()
                        : user.getEmail();
            dto.setUserName(display);
        }

        List<HomeSwapResponse.PhotoDto> photos = new ArrayList<>();
        if (e.getPhotos() != null) {
            for (HomeSwapPhoto p : e.getPhotos()) {
                HomeSwapResponse.PhotoDto pd = new HomeSwapResponse.PhotoDto();
                pd.setId(p.getId());
                pd.setUrl(p.getUrl());
                pd.setWidth(p.getWidth());
                pd.setHeight(p.getHeight());
                pd.setSortOrder(p.getSortOrder());
                photos.add(pd);
            }
        }
        dto.setPhotos(photos);

        return dto;
    }

    private static String safeName(MultipartFile f) {
        try {
            return f.getOriginalFilename();
        } catch (Exception ignore) {
            return "<unknown>";
        }
    }
}
