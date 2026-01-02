package com.habesha.community.controller;

import com.habesha.community.dto.UpdateReportStatusRequest;
import com.habesha.community.dto.UserReportRequest;
import com.habesha.community.dto.UserReportResponse;
import com.habesha.community.service.UserReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Reporting API.
 * - Normal users: POST /api/reports/user
 * - Mods/Admins : GET /api/reports, PATCH /api/reports/{id}/status
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserReportService userReportService;

    /**
     * Submit a user report.
     * body: { "targetUserId": 123, "reason": "scam / spam / ..." }
     */
    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> reportUser(
            @Valid @RequestBody UserReportRequest request
    ) {
        userReportService.submitReport(request);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * List recent OPEN + REVIEWED reports for moderation.
     * Only for admins/moderators.
     */
    @GetMapping
    public ResponseEntity<List<UserReportResponse>> getReportsForModeration() {
        List<UserReportResponse> out = userReportService.listReportsForModeration();
        return ResponseEntity.ok(out);
    }

    /**
     * Update a report status to REVIEWED/CLOSED.
     * Only for admins/moderators.
     *
     * body: { "status": "REVIEWED" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserReportResponse> updateReportStatus(
            @PathVariable("id") Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest body
    ) {
        UserReportResponse updated = userReportService.updateStatus(reportId, body);
        return ResponseEntity.ok(updated);
    }
}
