package com.habesha.community.dto;



import com.habesha.community.model.UserReportStatus;

import lombok.Builder;

import lombok.Data;



import java.time.LocalDateTime;



@Data

@Builder

public class UserReportResponse {

    private Long id;



    private Long reporterId;

    private String reporterName;

    private String reporterUsername;

    private String reporterEmail;



    private Long targetId;

    private String targetName;

    private String targetUsername;

    private String targetEmail;



    private String reason;

    private UserReportStatus status;



    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}