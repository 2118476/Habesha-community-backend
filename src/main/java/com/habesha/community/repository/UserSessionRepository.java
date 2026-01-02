package com.habesha.community.repository;

import com.habesha.community.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    List<UserSession> findByUser_IdOrderByLastSeenDesc(Long userId);

    Optional<UserSession> findByToken(String token);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = ?1 AND s.id != ?2")
    void deleteAllByUserIdExceptCurrent(Long userId, Long currentSessionId);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < ?1")
    void deleteExpiredSessions(LocalDateTime now);

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.user.id = ?1")
    void deleteAllByUserId(Long userId);
}
