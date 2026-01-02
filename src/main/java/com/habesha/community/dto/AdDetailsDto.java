package com.habesha.community.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdDetailsDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private Boolean featured;
    private LocalDateTime createdAt;

    // poster info for "Contact Seller" / "View Profile"
    private Long posterId;
    private String posterName;
    private String posterAvatar;

    // like info
    private long likeCount;
    private boolean likedByMe;
}
