package com.habesha.community.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TravelPostResponse {
    private Long id;

    private String originCity;
    private String destinationCity;
    private LocalDate travelDate;

    private String message;
    private String contactMethod;

    private LocalDateTime createdAt;

    // Poster info
    private Long userId;
    private String userName;       // prefer real name, fallback to username/email
    private String userUsername;   // username for linking if needed
    private String userAvatar;     // profileImageUrl
}
