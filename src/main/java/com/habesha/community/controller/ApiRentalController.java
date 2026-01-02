package com.habesha.community.controller;

import com.habesha.community.dto.RentalDetailDto;
import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.Rental;
import com.habesha.community.repository.RentalRepository;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API controller for rental listings.  Supports pagination and
 * filtering by basic criteria.  Returns enriched DTOs including
 * the owner's summary.
 */
@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class ApiRentalController {

    private final RentalRepository rentalRepository;
    private final UserService userService;

    /**
     * List rentals with optional filters.  Only page/size and sort are
     * currently supported; search parameters are reserved for future use.
     */
    @GetMapping
    public ResponseEntity<List<RentalDetailDto>> listRentals(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "type", required = false) String roomType,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "location", required = false) String location,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,DESC") String sort
    ) {
        Sort.Direction dir = Sort.Direction.DESC;
        String sortProp = "createdAt";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortProp = parts[0];
            dir = parts[1].equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortProp));
        // For now, ignore search and filtering; they can be implemented later
        Page<Rental> pageData = rentalRepository.findAll(pageable);
        List<RentalDetailDto> dtos = pageData.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get details for a single rental.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RentalDetailDto> getRental(@PathVariable Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Rental not found"));
        return ResponseEntity.ok(toDto(rental));
    }

    private RentalDetailDto toDto(Rental rental) {
        UserSummaryDto author = userService.toSummary(rental.getOwner());
        return RentalDetailDto.builder()
                .id(rental.getId())
                .title(rental.getTitle())
                .description(rental.getDescription())
                .price(rental.getPrice())
                .currency("GBP")
                .deposit(null)
                .roomType(rental.getRoomType())
                .location(rental.getLocation())
                .amenities(Collections.emptyList())
                .featured(rental.isFeatured())
                .images(rental.getImages() != null ? rental.getImages() : Collections.emptyList())
                .createdAt(rental.getCreatedAt())
                .postedBy(author)
                .author(author)
                .build();
    }


    /**
     * Create a new Rental for the current user.
     */
    @PostMapping
    public ResponseEntity<Rental> createRental(@RequestBody Rental body) {
        var me = userService.getCurrentUser().orElseThrow(() -> 
            new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        body.setOwner(me);
        var saved = rentalRepository.save(body);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update an existing Rental owned by the current user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Rental> updateRental(@PathVariable Long id, @RequestBody Rental body) {
        var me = userService.getCurrentUser().orElseThrow(() -> 
            new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        var existing = rentalRepository.findById(id).orElseThrow(() -> 
            new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        // Only owner can update
        if (existing.getOwner() != null && !existing.getOwner().getId().equals(me.getId())) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN);
        }
        body.setId(existing.getId());
        body.setOwner(me);
        var saved = rentalRepository.save(body);
        return ResponseEntity.ok(saved);
    }

}
