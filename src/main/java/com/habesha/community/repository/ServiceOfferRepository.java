package com.habesha.community.repository;

import com.habesha.community.model.ServiceOffer;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    List<ServiceOffer> findByCategoryIgnoreCase(String category);
    
    // Ordered methods for recent posts first
    List<ServiceOffer> findAllByOrderByCreatedAtDesc();
    List<ServiceOffer> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);

    // Correct nested property
    List<ServiceOffer> findByProvider_Id(Long providerId);
    List<ServiceOffer> findByProvider_IdOrderByCreatedAtDesc(Long providerId);

    // (Optional convenience)
    List<ServiceOffer> findByProvider(User provider);

    /**
     * Count the number of service offers provided by a given user.
     * Use nested property reference (`provider.id`) in the method name.
     */
    long countByProvider_Id(Long providerId);
}
