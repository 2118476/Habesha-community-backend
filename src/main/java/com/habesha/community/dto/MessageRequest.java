package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for sending a private message to another user.  The
 * sender is derived from the authenticated principal.  The client
 * can optionally request that the message also be delivered via SMS
 * by setting {@code viaSms} to true.
 */
@Data
public class MessageRequest {
    @NotNull
    private Long recipientId;
    @NotBlank
    private String content;
    private boolean viaSms;
}