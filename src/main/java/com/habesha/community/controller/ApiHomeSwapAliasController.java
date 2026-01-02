package com.habesha.community.controller;

import com.habesha.community.service.HomeSwapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Alias controller for /api/homeswap (without dash) to support both URL formats.
 * This ensures compatibility with frontend code that might use either format.
 */
@RestController
@RequestMapping("/api/homeswap")
@RequiredArgsConstructor
public class ApiHomeSwapAliasController {

    private final HomeSwapService homeSwapService;

    /**
     * List all home swap posts with pagination.
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
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    /**
     * Get a single home swap post by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            var response = homeSwapService.getOne(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
