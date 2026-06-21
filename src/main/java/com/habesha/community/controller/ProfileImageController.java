package com.habesha.community.controller;

import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.User;
import com.habesha.community.service.SupabaseStorageService;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileImageController {

    private final UserService userService;
    private final SupabaseStorageService supabaseStorage;

    /**
     * Upload avatar. Accepts common field names.
     * When Supabase Storage is configured, the bytes go there and only the CDN URL is stored in Postgres.
     */
    @PostMapping(
        path = "/me/profile-image",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadProfileImageToDb(
            @RequestParam(value = "file",         required = false) MultipartFile file,
            @RequestParam(value = "image",        required = false) MultipartFile image,
            @RequestParam(value = "avatar",       required = false) MultipartFile avatar,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        MultipartFile part =
            notEmpty(file) ? file :
            notEmpty(image) ? image :
            notEmpty(avatar) ? avatar :
            profileImage;

        if (!notEmpty(part)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No file provided. Send multipart/form-data with field file|image|avatar|profileImage");
        }

        final String contentType = Objects.toString(part.getContentType(), "image/jpeg");
        final byte[] bytes;
        try {
            bytes = part.getBytes();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read uploaded file");
        }
        if (bytes.length > 10 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Max 10MB");
        }

        UserResponse updated;
        try {
            if (supabaseStorage.isEnabled()) {
                User currentUser = userService.getCurrentUser()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
                String cdnUrl = supabaseStorage.upload(
                    "profile/" + currentUser.getId(),
                    Objects.requireNonNullElse(part.getOriginalFilename(), "avatar"),
                    bytes,
                    contentType
                );
                // updateProfileImageUrl clears the blob and sets the external URL
                updated = userService.updateProfileImageUrl(cdnUrl);
            } else {
                updated = userService.updateProfileImage(bytes, contentType);
            }
        } catch (Exception e) {
            log.error("Failed to save profile image: type={}, size={}, error={}",
                    contentType, bytes.length, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save profile image: " + e.getMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("user", updated);
        return ResponseEntity.ok(body);
    }

    /**
     * Stream or redirect to the current user's avatar.
     */
    @GetMapping("/me/profile-image")
    public ResponseEntity<?> getMyProfileImage() {
        User u = userService.getCurrentUser()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not authenticated"));
        String imgUrl = u.getProfileImageUrl();
        if (imgUrl != null && (imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(imgUrl))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .build();
        }
        var img = userService.getCurrentUserImage()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No image"));
        return buildImageResponse(img.data(), img.contentType());
    }

    /**
     * Public endpoint to fetch a user's avatar by ID (useful for {@code <img>} tags).
     */
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<?> getUserProfileImage(@PathVariable Long id) {
        // Redirect to CDN if there's an external URL (Supabase path)
        try {
            User u = userService.getEntityById(id);
            String imgUrl = u.getProfileImageUrl();
            if (imgUrl != null && (imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(imgUrl))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                        .build();
            }
        } catch (Exception ignored) {}

        var img = userService.getUserImageById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No image"));
        return buildImageResponse(img.data(), img.contentType());
    }

    private static boolean notEmpty(MultipartFile f) {
        return f != null && !f.isEmpty();
    }

    private static ResponseEntity<byte[]> buildImageResponse(byte[] bytes, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            contentType != null ? contentType : "image/jpeg"));
        headers.setCacheControl(CacheControl.noCache());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    /** Remove the current user's profile image (sets fields to null). */
    @DeleteMapping("/me/profile-image")
    public ResponseEntity<Void> deleteMyProfileImage() {
        userService.removeProfileImage();
        return ResponseEntity.noContent().build();
    }
}
