package com.habesha.community.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactRequestCreateRequest {
    @NotNull private Long targetUserId;
    @NotNull private String type; // "email" | "phone"
}
