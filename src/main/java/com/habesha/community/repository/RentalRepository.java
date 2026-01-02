package com.habesha.community.repository;

import com.habesha.community.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByLocationIgnoreCase(String location);
    
    // Ordered methods for recent posts first
    List<Rental> findAllByOrderByCreatedAtDesc();
    List<Rental> findByLocationIgnoreCaseOrderByCreatedAtDesc(String location);

    /**
     * Count rentals owned by a particular user.  Use nested property reference (`owner.id`).
     */
    long countByOwner_Id(Long ownerId);

    /**
     * Find rentals by owner.  Useful for counting and listing a user's listings.  Use nested property reference (`owner.id`).
     */
    List<Rental> findByOwner_Id(Long ownerId);
    List<Rental> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);
}