package com.habesha.community.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDTO {
    private Long id;
    private String name;
    private String email;

    // ✅ Add this custom constructor
    public SimpleUserDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
