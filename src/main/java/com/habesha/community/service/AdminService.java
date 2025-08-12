package com.habesha.community.service;

import com.habesha.community.model.Payment;
import com.habesha.community.model.Role;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.model.User;
import com.habesha.community.repository.PaymentRepository;
import com.habesha.community.repository.ServiceOfferRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides administrative functions such as adjusting commission rates,
 * viewing system statistics and modifying user roles.  All methods
 * here should be secured at the controller level to require
 * {@link Role#ADMIN}.
 */
@Service
@RequiredArgsConstructor
public class AdminService {
    private final ServiceMarketplaceService marketplaceService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ServiceOfferRepository offerRepository;

    /**
     * Updates the commission rate used for service bookings.
     */
    public void updateCommission(BigDecimal percentage) {
        marketplaceService.setCommissionRate(percentage);
    }

    /**
     * Returns simple statistics about the platform: total users,
     * total providers, total income from succeeded payments.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = userRepository.count();
        long providers = userRepository.findByRole(Role.SERVICE_PROVIDER).size();
        BigDecimal totalIncome = paymentRepository.findByStatus(com.habesha.community.model.PaymentStatus.SUCCEEDED)
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalUsers", totalUsers);
        stats.put("serviceProviders", providers);
        stats.put("totalIncome", totalIncome);
        stats.put("timestamp", LocalDateTime.now());
        return stats;
    }

    @Transactional
    public void updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<ServiceOffer> getAllServices() {
        return offerRepository.findAll();
    }
}