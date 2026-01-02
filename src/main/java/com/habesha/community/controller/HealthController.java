package com.habesha.community.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health check endpoint.
 *
 * <p>This controller exposes a lightweight endpoint that can be
 * queried by uptime monitoring services or platform health checks.
 * It returns a plain text response of "OK" when the application is
 * running. By permitting this endpoint without authentication, the
 * application can be pinged periodically to keep free hosting
 * platforms from idling the service.</p>
 */
@RestController
public class HealthController {

    /**
     * Health probe endpoint used to verify that the service is online.
     * Returns plain text "OK" with HTTP 200 status.
     * This endpoint performs no database checks or external calls to ensure reliability.
     *
     * @return a static "OK" string with plain text content type
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public String health() {
        return "OK";
    }
}