package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RegistrationResponse {
    private boolean success;
    private boolean verificationRequired;
    private String message;
    private String email;
}
