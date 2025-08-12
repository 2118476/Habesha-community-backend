package com.habesha.community.repository;

import com.habesha.community.model.Payment;
import com.habesha.community.model.PaymentStatus;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPayer(User payer);
    List<Payment> findByStatus(PaymentStatus status);
}