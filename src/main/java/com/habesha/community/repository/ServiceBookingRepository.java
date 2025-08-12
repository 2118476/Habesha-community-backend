package com.habesha.community.repository;

import com.habesha.community.model.ServiceBooking;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {
    List<ServiceBooking> findByCustomer(User customer);

    // Booking -> service (ServiceOffer) -> provider (User) -> id (Long)
    List<ServiceBooking> findByService_Provider_Id(Long providerId);
}
