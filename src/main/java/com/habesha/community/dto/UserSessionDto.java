package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDto {
    private Long id;
    private String device;
    private String ip;
    private LocalDateTime createdAt;
    private LocalDateTime lastSeen;
    private LocalDateTime expiresAt;
    private boolean current;  // true if this is the current session
}
