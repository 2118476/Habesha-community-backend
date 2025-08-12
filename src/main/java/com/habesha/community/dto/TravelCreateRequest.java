package com.habesha.community.dto;

import lombok.Data;

/**
 * Payload for creating a new travel post.
 * travelDate must be in YYYY-MM-DD format (ISO).
 */
@Data
public class TravelCreateRequest {
    private String originCity;
    private String destinationCity;
    private String travelDate; // parsed in service
    private String message;
    private String contactMethod;
}
