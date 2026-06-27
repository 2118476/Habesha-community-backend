package com.habesha.community.controller;

import com.habesha.community.model.AuditLog;
import com.habesha.community.model.UserReportStatus;
import com.habesha.community.repository.AuditLogRepository;
import com.habesha.community.repository.UserReportRepository;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.service.ModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trust &amp; Safety actions: content takedown, user suspend/warn, dashboard
 * overview counts, and the audit log. Restricted to moderators/admins.
 */
@RestController
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;
    private final AuditLogRepository auditLogRepository;
    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;

    private static Long asLong(Object o) {
        return o == null ? null : Long.valueOf(String.valueOf(o));
    }

    private static String asString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /** Remove a single piece of reported content. */
    @PostMapping("/api/mod/takedown")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> takedown(@RequestBody Map<String, Object> body) {
        moderationService.takedown(asString(body.get("contentType")), asLong(body.get("contentId")), asString(body.get("reason")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Suspend (ban) a user with a reason. */
    @PostMapping("/api/mod/users/{id}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> suspend(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, Object> body) {
        moderationService.suspend(id, body == null ? null : asString(body.get("reason")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Reinstate a suspended user. */
    @PostMapping("/api/mod/users/{id}/unsuspend")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> unsuspend(@PathVariable Long id) {
        moderationService.unsuspend(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Send a formal warning notice to a user. */
    @PostMapping("/api/mod/users/{id}/warn")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> warn(@PathVariable Long id,
                                                    @RequestBody(required = false) Map<String, Object> body) {
        moderationService.warn(id, body == null ? null : asString(body.get("message")));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** Dashboard overview counters. */
    @GetMapping("/api/mod/overview")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Map<String, Object>> overview() {
        Map<String, Object> m = new HashMap<>();
        m.put("openReports", userReportRepository.countByStatusIn(List.of(UserReportStatus.OPEN, UserReportStatus.REVIEWED)));
        m.put("reportsToday", userReportRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay()));
        m.put("totalUsers", userRepository.count());
        return ResponseEntity.ok(m);
    }

    /** Audit log (admin only). */
    @GetMapping("/api/admin/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLog>> audit(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }
}
