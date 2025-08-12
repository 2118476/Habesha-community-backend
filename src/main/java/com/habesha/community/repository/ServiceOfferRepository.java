package com.habesha.community.repository;

import com.habesha.community.model.ServiceOffer;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOfferRepository extends JpaRepository<ServiceOffer, Long> {
    List<ServiceOffer> findByCategoryIgnoreCase(String category);

    // Correct nested property
    List<ServiceOffer> findByProvider_Id(Long providerId);

    // (Optional convenience)
    List<ServiceOffer> findByProvider(User provider);
}
