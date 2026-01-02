package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnsureThreadResponse {
    private Long threadUserId; // the other participant id; front-end routes by user id
    private UserSummaryDto to;
}
