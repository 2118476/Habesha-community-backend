package com.habesha.community.controller;

import com.habesha.community.dto.TravelCreateRequest;
import com.habesha.community.dto.TravelPostResponse;
import com.habesha.community.service.TravelService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    /* -------------------- CREATE -------------------- */
    @PostMapping({"/travel", "/api/travel"})
    public ResponseEntity<TravelPostResponse> create(
            @RequestBody TravelCreateRequest req
    ) {
        TravelPostResponse created = travelService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* -------------------- LIST / SEARCH -------------------- */
    /**
     * List or search travel posts.
     *
     * Supports optional pagination parameters (page/size or pageNumber/pageSize).
     * If none are provided, returns the full list of results sorted newest-first.
     * Unrecognized query params are ignored.
     *
     * Used by:
     *  - Travel list page
     *  - Global search (calls /api/travel?page=0&size=36)
     */
    @GetMapping({"/travel", "/api/travel"})
    public ResponseEntity<List<TravelPostResponse>> list(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false, name = "pageNumber") Integer pageNumber,
            @RequestParam(required = false, name = "pageSize") Integer pageSize
    ) {
        // Backend search by origin/destination/date
        List<TravelPostResponse> all = travelService.search(
                Optional.ofNullable(origin),
                Optional.ofNullable(destination),
                Optional.ofNullable(date)
        );

        if (all.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Default: full list when no pagination params are given
        int p = 0;
        int s = all.size();
        if (page != null) {
            p = Math.max(0, page);
        } else if (pageNumber != null) {
            p = Math.max(0, pageNumber);
        }
        if (size != null) {
            s = Math.max(1, size);
        } else if (pageSize != null) {
            s = Math.max(1, pageSize);
        }

        // Hard cap to avoid huge pages (defensive)
        s = Math.min(s, 100);

        int fromIndex = p * s;
        if (fromIndex >= all.size()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        int toIndex = Math.min(all.size(), fromIndex + s);
        List<TravelPostResponse> paged = all.subList(fromIndex, toIndex);

        return ResponseEntity.ok(paged);
    }

    /* -------------------- GET ONE -------------------- */
    @GetMapping({"/travel/{id}", "/api/travel/{id}"})
    public ResponseEntity<TravelPostResponse> getOne(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(travelService.getOne(id));
    }

    /**
     * Lightweight HEAD endpoint so the frontend search can quickly
     * verify a travel post still exists (used by filterExistingItems).
     */
    @RequestMapping(value = {"/travel/{id}", "/api/travel/{id}"}, method = RequestMethod.HEAD)
    public ResponseEntity<Void> headOne(@PathVariable Long id) {
        try {
            travelService.getOne(id);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /* -------------------- UPDATE -------------------- */
    @PutMapping({"/travel/{id}", "/api/travel/{id}"})
    public ResponseEntity<TravelPostResponse> update(
            @PathVariable Long id,
            @RequestBody TravelCreateRequest req
    ) {
        return ResponseEntity.ok(travelService.update(id, req));
    }

    /* -------------------- DELETE -------------------- */
    @DeleteMapping({"/travel/{id}", "/api/travel/{id}"})
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        travelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
