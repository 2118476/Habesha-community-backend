package com.habesha.community.repository;

import com.habesha.community.model.UserReport;
import com.habesha.community.model.UserReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DB access for reports.
 */
@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    /**
     * Used for spam-throttling:
     * "Did the same reporter report the same target with the same reason recently?"
     */
    Optional<UserReport> findTopByReporter_IdAndTarget_IdAndReasonIgnoreCaseOrderByCreatedAtDesc(
            Long reporterId,
            Long targetId,
            String reason
    );

    /**
     * Used by moderation dashboard: show newest/open first.
     */
    List<UserReport> findAllByStatusOrderByCreatedAtDesc(UserReportStatus status);

    /**
     * Used when moderator is browsing all reports (OPEN + REVIEWED).
     */
    List<UserReport> findAllByStatusInOrderByCreatedAtDesc(List<UserReportStatus> statuses);

    /**
     * Optional: list all reports involving a specific target user in last X minutes.
     */
    List<UserReport> findAllByTarget_IdAndCreatedAtAfter(Long targetId, LocalDateTime after);
}
