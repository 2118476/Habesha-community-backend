package com.habesha.community.dto;

import lombok.Data;

/** Payload for registering/unregistering a device push token. */
@Data
public class RegisterTokenRequest {
    private String token;
    /** ANDROID | IOS | WEB */
    private String platform;
}
