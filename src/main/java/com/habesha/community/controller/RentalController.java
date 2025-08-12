package com.habesha.community.controller;

import com.habesha.community.dto.RentalRequest;
import com.habesha.community.model.Rental;
import com.habesha.community.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints for housing and rental listings.
 */
@RestController
@RequestMapping("/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<Rental> createRental(@Valid @RequestBody RentalRequest request) {
        return ResponseEntity.ok(rentalService.createRental(request));
    }

    @GetMapping
    public ResponseEntity<List<Rental>> listRentals(@RequestParam(name = "city", required = false) String city) {
        return ResponseEntity.ok(rentalService.listRentals(Optional.ofNullable(city)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rental> getRental(@PathVariable Long id) {
        return ResponseEntity.ok(rentalService.getRental(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        rentalService.deleteRental(id);
        return ResponseEntity.noContent().build();
    }
}