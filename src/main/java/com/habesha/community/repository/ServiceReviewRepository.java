package com.habesha.community.repository;

import com.habesha.community.model.ServiceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReview, Long> {

    /** Newest reviews for a provider. */
    List<ServiceReview> findByProvider_IdOrderByCreatedAtDesc(Long providerId);

    /** A specific reviewer's existing review for a provider (if any). */
    Optional<ServiceReview> findByProvider_IdAndReviewer_Id(Long providerId, Long reviewerId);

    long countByProvider_Id(Long providerId);

    /** Average rating for a provider (null when there are no reviews). */
    @Query("select avg(r.rating) from ServiceReview r where r.provider.id = :providerId")
    Double averageRating(Long providerId);
}
