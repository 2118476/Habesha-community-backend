package com.habesha.community.service;

import com.habesha.community.dto.ServiceBookingRequest;
import com.habesha.community.dto.ServiceOfferRequest;
import com.habesha.community.model.BookingStatus;
import com.habesha.community.model.Role;
import com.habesha.community.model.ServiceBooking;
import com.habesha.community.model.ServiceOffer;
import com.habesha.community.model.User;
import com.habesha.community.repository.ServiceBookingRepository;
import com.habesha.community.repository.ServiceOfferRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Handles creation and booking of services on the marketplace.  A
 * configurable commission rate is applied to each booking.
 */
@Service
@RequiredArgsConstructor
public class ServiceMarketplaceService {
    // Commission rate in percentage (e.g. 15 means 15%)
    private BigDecimal commissionRate = BigDecimal.valueOf(15);

    private final ServiceOfferRepository offerRepository;
    private final ServiceBookingRepository bookingRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new IllegalStateException("No current user"));
    }

    @Transactional
    public ServiceOffer createService(ServiceOfferRequest request) {
        User provider = getCurrentUser();
        if (provider.getRole() != Role.SERVICE_PROVIDER && provider.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Only service providers can create services");
        }
        ServiceOffer offer = ServiceOffer.builder()
                .provider(provider)
                .category(request.getCategory())
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedTime(request.getEstimatedTime())
                .basePrice(request.getBasePrice())
                .location(request.getLocation())
                .mode(request.getMode())
                .featured(request.isFeatured())
                .build();
        return offerRepository.save(offer);
    }

    public List<ServiceOffer> listServices(Optional<String> category) {
        return category.map(offerRepository::findByCategoryIgnoreCase).orElseGet(offerRepository::findAll);
    }

    @Transactional
    public ServiceBooking bookService(ServiceBookingRequest request) {
        User customer = getCurrentUser();
        ServiceOffer offer = offerRepository.findById(request.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found"));
        BigDecimal basePrice = offer.getBasePrice();
        BigDecimal commission = basePrice.multiply(commissionRate).divide(BigDecimal.valueOf(100));
        BigDecimal totalPrice = basePrice.add(commission);
        ServiceBooking booking = ServiceBooking.builder()
                .service(offer)
                .customer(customer)
                .amountPaid(totalPrice)
                .status(BookingStatus.PENDING)
                .build();
        return bookingRepository.save(booking);
    }

    public List<ServiceBooking> getBookingsForCurrentUser() {
        User current = getCurrentUser();
        return bookingRepository.findByCustomer(current);
    }

    public List<ServiceBooking> getBookingsForProvider() {
        User provider = getCurrentUser();
        return bookingRepository.findByService_Provider_Id(provider.getId());

    }

    @Transactional
    public void deleteService(Long id) {
        User current = getCurrentUser();
        ServiceOffer offer = offerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found"));
        if (!offer.getProvider().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorised to delete this service");
        }
        offerRepository.delete(offer);
    }

    public void setCommissionRate(BigDecimal rate) {
        this.commissionRate = rate;
    }
}