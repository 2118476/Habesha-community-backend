package com.habesha.community.controller;

import com.habesha.community.model.Payment;
import com.habesha.community.model.Role;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.service.AdminService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Administrative endpoints.  All methods require the ADMIN role.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @PutMapping("/commission")
    public ResponseEntity<Void> updateCommission(@RequestParam @Positive BigDecimal rate) {
        adminService.updateCommission(rate);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        adminService.updateUserRole(id, role);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceOffer>> getServices() {
        return ResponseEntity.ok(adminService.getAllServices());
    }
}