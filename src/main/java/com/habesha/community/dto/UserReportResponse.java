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



    /** What was reported (USER, RENTAL, SERVICE, ...) and its id. */

    private String contentType;

    private Long contentId;



    /** How many OPEN/REVIEWED reports exist against this same content/user. */

    private Long reportCount;



    /** Whether the target user is currently active (false = suspended/banned). */

    private Boolean targetActive;



    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}