package com.habesha.community.dto;



import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;

import lombok.Data;

import lombok.NoArgsConstructor;



/**

 * Request body for reporting a user.

 */

@Data

@NoArgsConstructor

@AllArgsConstructor

public class UserReportRequest {



    @NotNull

    private Long targetUserId;



    @NotBlank

    @Size(max = 500)

    private String reason;



    /** Optional: what was reported (USER, RENTAL, SERVICE, ...). Defaults to USER. */

    private String contentType;



    /** Optional: id of the reported content (defaults to targetUserId for USER). */

    private Long contentId;

}