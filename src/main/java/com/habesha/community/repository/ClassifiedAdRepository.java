package com.habesha.community.repository;

import com.habesha.community.model.ClassifiedAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassifiedAdRepository extends JpaRepository<ClassifiedAd, Long> {
    List<ClassifiedAd> findByCategoryIgnoreCase(String category);
    
    // Ordered methods for recent posts first
    List<ClassifiedAd> findAllByOrderByCreatedAtDesc();
    List<ClassifiedAd> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);

    /**
     * Retrieve all ads posted by a particular user. Spring Data will
     * automatically translate this method name into the appropriate
     * query using the poster_id foreign key.
     */
    List<ClassifiedAd> findByPoster_Id(Long posterId);
    List<ClassifiedAd> findByPoster_IdOrderByCreatedAtDesc(Long posterId);

    /**
     * Find an ad by ID with photos eagerly loaded.
     */
    @Query("SELECT a FROM ClassifiedAd a LEFT JOIN FETCH a.photos WHERE a.id = :id")
    Optional<ClassifiedAd> findByIdWithPhotos(@Param("id") Long id);
}