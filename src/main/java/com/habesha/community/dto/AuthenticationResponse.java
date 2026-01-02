package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response returned when a user logs in successfully or
 * registers a new account.  The token field contains the JWT
 * which must be supplied as a Bearer token on subsequent requests.
 */
@Data
@AllArgsConstructor
public class AuthenticationResponse {
    /**
     * The JWT token that must be supplied as a Bearer token on
     * subsequent requests.
     */
    private String token;
    /**
     * Basic user information returned on login and registration.  This
     * allows the frontend to hydrate initial user state without
     * performing an additional request.
     */
    private com.habesha.community.dto.UserResponse user;
}