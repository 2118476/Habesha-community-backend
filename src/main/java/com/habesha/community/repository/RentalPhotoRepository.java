package com.habesha.community.repository;

import com.habesha.community.model.RentalPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalPhotoRepository extends JpaRepository<RentalPhoto, Long> {
    List<RentalPhoto> findByRental_IdOrderBySortIndexAscIdAsc(Long rentalId);
}
