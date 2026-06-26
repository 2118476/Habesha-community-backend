package com.habesha.community.service;

import com.habesha.community.dto.RentalDetailDto;
import com.habesha.community.dto.RentalRequest;
import com.habesha.community.dto.RentalUpdateRequest;
import com.habesha.community.dto.UserSummaryDto;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for posting, editing, and searching rental listings.
 */
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    /**
     * Resolve the authenticated User from the security context.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No current user"));
    }

    /** Parse an ISO yyyy-MM-dd string to LocalDate; null/blank/invalid -> null. */
    private static java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.time.LocalDate.parse(s.trim());
        } catch (Exception ignore) {
            return null;
        }
    }

    /* ===================== CREATE ===================== */

    @Transactional
    public Rental createRental(RentalRequest request) {
        User owner = getCurrentUser();

        Rental rental = Rental.builder()
                .owner(owner)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .price(request.getPrice()) // assuming request.getPrice() is already BigDecimal
                .roomType(request.getRoomType())
                .contact(request.getContact())
                .images(
                        Optional.ofNullable(request.getImages())
                                .orElse(List.of())
                )
                .featured(request.isFeatured())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .deposit(request.getDeposit())
                .billsIncluded(request.getBillsIncluded())
                .furnished(request.getFurnished())
                .availableFrom(parseDate(request.getAvailableFrom()))
                .build();

        return rentalRepository.save(rental);
    }

    /* ===================== LIST ===================== */

    public List<Rental> listRentals(Optional<String> city) {
        return city
                .map(rentalRepository::findByLocationIgnoreCaseOrderByCreatedAtDesc)
                .orElseGet(rentalRepository::findAllByOrderByCreatedAtDesc);
    }

    /**
     * List rentals as DTOs. Runs inside a transaction so the mapping can read
     * the entity safely (open-in-view is disabled), and returns a lightweight
     * payload that never serialises lazy collections or photo blobs.
     */
    @Transactional
    public List<RentalDetailDto> listRentalDtos(Optional<String> city) {
        return listRentals(city).stream()
                .map(r -> toDto(r, false))
                .collect(Collectors.toList());
    }

    /* ===================== READ ===================== */

    public Rental getRental(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
    }

    /**
     * Read a single rental as a DTO (includes owner summary). Transactional so
     * the lazy owner association can be resolved during mapping.
     */
    @Transactional
    public RentalDetailDto getRentalDto(Long id) {
        return toDto(getRental(id), true);
    }

    /**
     * Map a Rental entity to a response DTO. Only call this from within a
     * transaction. Photos are intentionally omitted here — the frontend loads
     * them via the dedicated /rentals/{id}/photos endpoints so we never dump
     * image bytes into list/detail JSON.
     */
    private RentalDetailDto toDto(Rental r, boolean includeOwner) {
        RentalDetailDto.RentalDetailDtoBuilder b = RentalDetailDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .description(r.getDescription())
                .price(r.getPrice())
                .currency("GBP")
                .roomType(r.getRoomType())
                .location(r.getLocation())
                .featured(r.isFeatured())
                .createdAt(r.getCreatedAt())
                .amenities(Collections.emptyList())
                .images(Collections.emptyList());

        if (includeOwner && r.getOwner() != null) {
            User o = r.getOwner();
            String displayName = (o.getName() != null && !o.getName().isBlank())
                    ? o.getName() : o.getUsername();
            b.ownerId(o.getId())
             .ownerName(displayName)
             .postedBy(UserSummaryDto.builder()
                     .id(o.getId())
                     .displayName(displayName)
                     .username(o.getUsername())
                     .build());
        }
        return b.build();
    }

    /* ===================== UPDATE ===================== */
    /**
     * Update editable fields:
     * - title, description, price, location
     * - roomType, contact, featured
     * Only the owner OR ADMIN can update.
     */
    @Transactional
    public Rental updateRental(Long id, RentalUpdateRequest req) {
        User me = getCurrentUser();

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        boolean isOwner = rental.getOwner() != null
                && rental.getOwner().getId() != null
                && rental.getOwner().getId().equals(me.getId());

        boolean isAdmin = (me.getRole() == Role.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not authorised to edit this rental");
        }

        // Apply updates if present
        if (req.getTitle() != null) {
            rental.setTitle(req.getTitle());
        }

        if (req.getDescription() != null) {
            rental.setDescription(req.getDescription());
        }

        if (req.getLocation() != null) {
            rental.setLocation(req.getLocation());
        }

        if (req.getRoomType() != null) {
            rental.setRoomType(req.getRoomType());
        }

        if (req.getContact() != null) {
            rental.setContact(req.getContact());
        }

        if (req.getFeatured() != null) {
            rental.setFeatured(req.getFeatured());
        }

        if (req.getPrice() != null) {
            try {
                // convert the string (e.g. "500") into BigDecimal
                BigDecimal parsed = new BigDecimal(req.getPrice().trim());
                rental.setPrice(parsed);
            } catch (NumberFormatException ignore) {
                // If price can't parse, skip updating price
            }
        }

        return rentalRepository.save(rental);
    }

    /* ===================== DELETE ===================== */
    /**
     * Only the owner OR ADMIN can delete.
     */
    @Transactional
    public void deleteRental(Long id) {
        User current = getCurrentUser();

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        boolean isOwner = rental.getOwner() != null
                && rental.getOwner().getId() != null
                && rental.getOwner().getId().equals(current.getId());

        boolean isAdmin = (current.getRole() == Role.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not authorised to delete this rental");
        }

        rentalRepository.delete(rental);
    }
}
