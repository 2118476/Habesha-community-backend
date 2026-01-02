// src/main/java/com/habesha/community/dto/AdCommentRequest.java
package com.habesha.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdCommentRequest {
    @NotBlank
    private String text;
}
