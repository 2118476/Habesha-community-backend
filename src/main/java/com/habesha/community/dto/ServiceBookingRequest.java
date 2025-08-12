package com.habesha.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for booking a service.  The ID of the service
 * being booked is passed in the path of the endpoint, while
 * additional fields can be extended in the future (e.g. notes
 * from the customer).
 */
@Data
public class ServiceBookingRequest {
    @NotNull
    private Long serviceId;
}