package com.habesha.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HomeSwapResponse {
    private Long id;
    private String title;
    private String location;
    private String description;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
    private String userUsername;
    private String userAvatar;

    private java.util.List<PhotoDto> photos;

@lombok.Data
public static class PhotoDto {
    private Long id;
    private String url;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
}
}
