package com.habesha.community.controller;

import com.habesha.community.dto.UserResponse;
import com.habesha.community.model.User;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileImageController {

    private final UserService userService;

    /**
     * Upload avatar into PostgreSQL (BYTEA). Accepts common field names.
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

        UserResponse updated = userService.updateProfileImage(bytes, contentType);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("user", updated);
        return ResponseEntity.ok(body);
    }

    /**
     * Stream the current user's avatar.
     */
    @GetMapping("/me/profile-image")
    public ResponseEntity<byte[]> getMyProfileImage() {
        var img = userService.getCurrentUserImage()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No image"));
        return buildImageResponse(img.data(), img.contentType());
    }

    /**
     * Public endpoint to fetch a user's avatar by ID (useful for <img> tags).
     */
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<byte[]> getUserProfileImage(@PathVariable Long id) {
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
        userService.removeProfileImage();      // no-op if already empty
        return ResponseEntity.noContent().build(); // 204
    }
}
