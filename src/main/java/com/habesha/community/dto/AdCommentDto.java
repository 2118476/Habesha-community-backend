// src/main/java/com/habesha/community/dto/AdCommentDto.java
package com.habesha.community.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdCommentDto {
    private Long id;
    private Long authorId;
    private String authorName;
    private Object authorAvatar;

    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // only top-level comments will include replies[]
    private List<AdCommentDto> replies;

    // permissions (so frontend can show Edit/Delete buttons)
    private boolean canEdit;
    private boolean canDelete;
}
