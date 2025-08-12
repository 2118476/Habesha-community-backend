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
    private String token;
}