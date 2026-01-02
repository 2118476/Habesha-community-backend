package com.habesha.community.controller;

import com.habesha.community.dto.EventDetailDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.Event;
import com.habesha.community.repository.EventRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API endpoints for listing and retrieving community events.  These
 * endpoints follow the new specification defined in the sprint brief
 * and return enriched DTOs that include organiser summaries.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class ApiEventController {

    private final EventRepository eventRepository;
    private final UserService userService;

    /**
     * List events with optional filters and pagination.  Unsupported
     * filters are currently ignored but reserved for future use.
     */
    @GetMapping
    public ResponseEntity<List<EventDetailDto>> listEvents(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,DESC") String sort
    ) {
        // Derive sort direction
        Sort.Direction dir = Sort.Direction.DESC;
        String sortProp = "createdAt";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortProp = parts[0];
            dir = parts[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortProp));
        Page<Event> eventPage = eventRepository.findAll(pageable);
        List<EventDetailDto> dtos = eventPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieve a single event by id.  Returns 404 if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventDetailDto> getEvent(@PathVariable Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Event not found"));
        return ResponseEntity.ok(toDto(event));
    }

    /**
     * Convert an Event entity into the detail DTO including author summary.
     */
    private EventDetailDto toDto(Event event) {
        UserSummaryDto author = userService.toSummary(event.getOrganizer());
        List<String> images = event.getImageUrl() != null && !event.getImageUrl().isBlank()
                ? Collections.singletonList(event.getImageUrl())
                : Collections.emptyList();
        return EventDetailDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(null)
                .date(event.getDate())
                .location(event.getLocation())
                .images(images)
                .createdAt(event.getCreatedAt())
                .postedBy(author)
                .author(author)
                .build();
    }


    /**
     * Create a new Event for the current user.
     */
    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event body) {
        var me = userService.getCurrentUser().orElseThrow(() -> 
            new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        body.setOrganizer(me);
        var saved = eventRepository.save(body);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing Event owned by the current user.
     */
  @PutMapping("/{id}")
public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event body) {
    var me = userService.getCurrentUser().orElseThrow(() ->
        new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
    var existing = eventRepository.findById(id).orElseThrow(() ->
        new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

    // Only the organiser can update
    if (existing.getOrganizer() != null && !existing.getOrganizer().getId().equals(me.getId())) {
        throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    body.setId(existing.getId());
    // Preserve original organiser if present, otherwise set to current user
    if (existing.getOrganizer() != null) {
        body.setOrganizer(existing.getOrganizer());
    } else {
        body.setOrganizer(me);
    }

    var saved = eventRepository.save(body);
    return ResponseEntity.ok(saved);
}


}
