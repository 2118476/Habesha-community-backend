package com.habesha.community.dto;

import com.habesha.community.model.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Data transfer object for returning basic user information via the API.
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String username; // ✅ NEW FIELD
    private String email;
    private String phone;
    private String city;
    private String profileImageUrl;
    private Role role;
    private Boolean frozen;
    private LocalDateTime createdAt; // ✅ NEW FIELD - Member since date
}
