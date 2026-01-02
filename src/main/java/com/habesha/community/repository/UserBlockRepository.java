package com.habesha.community.repository;

import com.habesha.community.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    List<UserBlock> findByBlocker_Id(Long blockerId);
    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
    Optional<UserBlock> findByIdAndBlocker_Id(Long id, Long blockerId);
    
    @org.springframework.data.jpa.repository.Query(
        "SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM UserBlock b " +
        "WHERE (b.blocker.id = :userAId AND b.blocked.id = :userBId) " +
        "OR (b.blocker.id = :userBId AND b.blocked.id = :userAId)"
    )
    boolean existsBidirectionalBlock(@org.springframework.data.repository.query.Param("userAId") Long userAId, 
                                     @org.springframework.data.repository.query.Param("userBId") Long userBId);
}
