package com.habesha.community.service;

import com.habesha.community.dto.MessageRequest;
import com.habesha.community.dto.UpdateReportStatusRequest;
import com.habesha.community.dto.UserReportRequest;
import com.habesha.community.dto.UserReportResponse;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.model.UserReport;
import com.habesha.community.model.UserReportStatus;
import com.habesha.community.repository.UserReportRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReportService {

    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;
    private final MessageService messageService;

    /* ---------------- helpers ---------------- */

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null ||
            !auth.isAuthenticated() ||
            "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("User is not authenticated");
        }

        // IMPORTANT: adjust this finder if you authenticate by username instead of email.
        return userRepository.findByEmail(auth.getName())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private boolean isModeratorOrAdmin(User u) {
        if (u == null || u.getRole() == null) return false;
        return u.getRole() == Role.ADMIN || u.getRole() == Role.MODERATOR;
    }

    private String displayName(User u) {
        if (u == null) return "unknown";
        if (u.getName() != null && !u.getName().isBlank()) return u.getName();
        if (u.getUsername() != null && !u.getUsername().isBlank()) return u.getUsername();
        return u.getEmail();
    }

    private UserReportResponse toResponse(UserReport r) {
        return UserReportResponse.builder()
            .id(r.getId())

            .reporterId(r.getReporter().getId())
            .reporterName(displayName(r.getReporter()))
            .reporterUsername(r.getReporter().getUsername())
            .reporterEmail(r.getReporter().getEmail())

            .targetId(r.getTarget().getId())
            .targetName(displayName(r.getTarget()))
            .targetUsername(r.getTarget().getUsername())
            .targetEmail(r.getTarget().getEmail())

            .reason(r.getReason())
            .status(r.getStatus())

            .createdAt(r.getCreatedAt())
            .updatedAt(r.getUpdatedAt())
            .build();
    }

    /* ---------------- main actions ---------------- */

    /**
     * Normal user submits report about another user.
     * We:
     *   - throttle spam (same report repeatedly)
     *   - save it
     *   - alert admins/moderators via DM
     */
    @Transactional
    public void submitReport(UserReportRequest req) {
        if (req.getTargetUserId() == null) {
            throw new IllegalArgumentException("targetUserId is required");
        }
        if (req.getReason() == null || req.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }

        User reporter = getCurrentUserOrThrow();

        if (reporter.getId().equals(req.getTargetUserId())) {
            throw new IllegalArgumentException("You cannot report yourself.");
        }

        User target = userRepository.findById(req.getTargetUserId())
            .orElseThrow(() -> new IllegalArgumentException("Reported user not found"));

        String reasonClean = req.getReason().trim();

        // --- spam throttle: prevent hammering same report every few seconds ---
        userReportRepository
            .findTopByReporter_IdAndTarget_IdAndReasonIgnoreCaseOrderByCreatedAtDesc(
                reporter.getId(),
                target.getId(),
                reasonClean
            )
            .ifPresent(latest -> {
                // If the last identical complaint is less than 10min old and still OPEN,
                // don't create a new one (just silently succeed).
                boolean sameStatusOpen = latest.getStatus() == UserReportStatus.OPEN;
                long mins = Duration.between(latest.getCreatedAt(), LocalDateTime.now()).toMinutes();
                if (sameStatusOpen && mins < 10) {
                    // still notify moderators once at first submit, but not again within 10 min
                    return;
                }
            });

        // --- save new row ---
        UserReport saved = userReportRepository.save(
            UserReport.builder()
                .reporter(reporter)
                .target(target)
                .reason(reasonClean)
                .status(UserReportStatus.OPEN)
                .build()
        );

        // --- DM every ADMIN and MODERATOR with details ---
        String alertText =
            "🚩 USER REPORT\n" +
            "From: " + displayName(reporter) + " (id " + reporter.getId() + ")\n" +
            "Against: " + displayName(target)   + " (id " + target.getId()   + ")\n" +
            "Reason: " + reasonClean + "\n" +
            "Report ID: " + saved.getId();

        for (User u : userRepository.findAll()) {
            if (isModeratorOrAdmin(u)) {
                if (!u.getId().equals(reporter.getId())) {
                    try {
                        MessageRequest msg = new MessageRequest();
                        // NOTE: adjust field names if your MessageRequest is different
                        msg.setRecipientId(u.getId());
                        msg.setContent(alertText);
                        msg.setViaSms(false);

                        messageService.sendMessage(msg);
                    } catch (Exception ignore) {
                        // don't nuke the report if one DM fails
                    }
                }
            }
        }
    }

    /**
     * For moderation dashboard:
     * Return all OPEN + REVIEWED reports newest first.
     * Only moderators/admins can call this.
     */
    @Transactional
    public List<UserReportResponse> listReportsForModeration() {
        User me = getCurrentUserOrThrow();
        if (!isModeratorOrAdmin(me)) {
            throw new AccessDeniedException("Not allowed");
        }

        List<UserReport> rows = userReportRepository.findAllByStatusInOrderByCreatedAtDesc(
            Arrays.asList(UserReportStatus.OPEN, UserReportStatus.REVIEWED)
        );
        return rows.stream().map(this::toResponse).toList();
    }

    /**
     * Moderator/admin can update status (REVIEWED or CLOSED).
     */
    @Transactional
    public UserReportResponse updateStatus(Long reportId, UpdateReportStatusRequest body) {
        if (reportId == null) {
            throw new IllegalArgumentException("reportId is required");
        }
        if (body == null || body.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }

        User me = getCurrentUserOrThrow();
        if (!isModeratorOrAdmin(me)) {
            throw new AccessDeniedException("Not allowed");
        }

        UserReport r = userReportRepository.findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        r.setStatus(body.getStatus()); // REVIEWED or CLOSED
        UserReport saved = userReportRepository.save(r);

        return toResponse(saved);
    }
}
