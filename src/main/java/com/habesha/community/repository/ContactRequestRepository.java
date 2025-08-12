package com.habesha.community.repository;

import com.habesha.community.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    boolean existsByRequesterIdAndTargetIdAndTypeAndStatus(Long requesterId, Long targetId, ContactType type, ContactRequestStatus status);
    List<ContactRequest> findByTargetIdAndStatus(Long targetId, ContactRequestStatus status);
    List<ContactRequest> findByRequesterId(Long requesterId);
}
