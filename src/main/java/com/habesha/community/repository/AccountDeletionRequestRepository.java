package com.habesha.community.repository;

import com.habesha.community.model.AccountDeletionRequest;
import com.habesha.community.model.AccountDeletionRequest.AccountDeletionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountDeletionRequestRepository extends JpaRepository<AccountDeletionRequest, Long> {

    Optional<AccountDeletionRequest> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    Page<AccountDeletionRequest> findByStatusOrderByCreatedAtDesc(AccountDeletionStatus status, Pageable pageable);
}