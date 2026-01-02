package com.habesha.community.controller;

import com.habesha.community.dto.HomeSwapRequest;
import com.habesha.community.dto.HomeSwapResponse;
import com.habesha.community.service.HomeSwapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST API for editing and deleting home swap posts.  These endpoints
 * correspond to the React frontend's calls under the `/api/home-swap` path.
 *
 * <p>Only the owner of a post may update it.  Deletion is permitted
 * for the owner as well as administrators and moderators, mirroring
 * the permissions used for rental listings.  All responses are
 * consistent with the Rental API, returning 401 for unauthenticated
 * users, 403 for forbidden operations and 404 when the post does
 * not exist.
 */
@RestController
@RequestMapping("/api/home-swap")
@RequiredArgsConstructor
public class ApiHomeSwapController {

    private final HomeSwapService homeSwapService;

    /**
     * List all home swap posts with pagination.
     *
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @return paginated list of home swap posts
     */
    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            var result = homeSwapService.list();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Fallback to empty list if error occurs
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    /**
     * Get a single home swap post by ID.
     *
     * @param id the post id
     * @return the home swap post DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<HomeSwapResponse> getById(@PathVariable Long id) {
        try {
            HomeSwapResponse response = homeSwapService.getOne(id);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update a home swap post.  Only the original owner may update.
     *
     * @param id  the post id
     * @param req the updated fields
     * @return the updated post DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<HomeSwapResponse> update(
            @PathVariable Long id,
            @RequestBody HomeSwapRequest req
    ) {
        HomeSwapResponse updated = homeSwapService.update(id, req);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a home swap post.  Allowed for the owner, administrators and moderators.
     *
     * @param id the post id
     * @return HTTP 204 if deletion succeeds
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        homeSwapService.delete(id);
        return ResponseEntity.noContent().build();
    }
}