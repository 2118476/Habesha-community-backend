package com.habesha.community.controller;

import com.habesha.community.dto.ServiceBookingRequest;
import com.habesha.community.dto.ServiceOfferRequest;
import com.habesha.community.model.ServiceBooking;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.service.ServiceMarketplaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoints for the service marketplace.
 */
@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceMarketplaceService service;

    @PostMapping
    public ResponseEntity<ServiceOffer> createService(@Valid @RequestBody ServiceOfferRequest request) {
        return ResponseEntity.ok(service.createService(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOffer>> listServices(@RequestParam(name = "category", required = false) String category) {
        return ResponseEntity.ok(service.listServices(Optional.ofNullable(category)));
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<ServiceBooking> bookService(@PathVariable Long id) {
        ServiceBookingRequest req = new ServiceBookingRequest();
        req.setServiceId(id);
        return ResponseEntity.ok(service.bookService(req));
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<ServiceBooking>> getBookings() {
        // Return bookings for the current user (customer) or provider
        return ResponseEntity.ok(service.getBookingsForCurrentUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        service.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}