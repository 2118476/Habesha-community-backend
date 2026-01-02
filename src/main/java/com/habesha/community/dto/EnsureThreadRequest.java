package com.habesha.community.dto;

import lombok.Data;

@Data
public class EnsureThreadRequest {
    private Long userId;
    private String contextType; // optional: event|service|travel|rental
    private Long contextId;     // optional
}
