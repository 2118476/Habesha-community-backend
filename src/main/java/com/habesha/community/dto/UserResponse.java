package com.habesha.community.dto;

import com.habesha.community.model.Role;
import lombok.Builder;
import lombok.Data;

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
}
