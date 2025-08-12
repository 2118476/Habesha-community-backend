package com.habesha.community.service;

import com.habesha.community.dto.RentalRequest;
import com.habesha.community.model.Role;
import com.habesha.community.model.Rental;
import com.habesha.community.model.User;
import com.habesha.community.repository.RentalRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for posting and searching rental listings.
 */
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new IllegalStateException("No current user"));
    }

    @Transactional
    public Rental createRental(RentalRequest request) {
        User owner = getCurrentUser();
        Rental rental = Rental.builder()
                .owner(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .price(request.getPrice())
                .roomType(request.getRoomType())
                .contact(request.getContact())
                .images(Optional.ofNullable(request.getImages()).orElse(List.of()))
                .featured(request.isFeatured())
                .build();
        return rentalRepository.save(rental);
    }

    public List<Rental> listRentals(Optional<String> city) {
        return city.map(rentalRepository::findByLocationIgnoreCase).orElseGet(rentalRepository::findAll);
    }

    public Rental getRental(Long id) {
        return rentalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rental not found"));
    }

    @Transactional
    public void deleteRental(Long id) {
        User current = getCurrentUser();
        Rental rental = rentalRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Rental not found"));
        if (!rental.getOwner().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorised to delete this rental");
        }
        rentalRepository.delete(rental);
    }
}