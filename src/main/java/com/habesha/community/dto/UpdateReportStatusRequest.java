package com.habesha.community.dto;

import com.habesha.community.model.UserReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Payload for PATCH /api/reports/{id}/status
 */
@Data
public class UpdateReportStatusRequest {

    @NotNull
    private UserReportStatus status;
}
