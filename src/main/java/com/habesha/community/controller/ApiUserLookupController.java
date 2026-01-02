package com.habesha.community.controller;

import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ApiUserLookupController {

    private final UserRepository userRepository;

    @GetMapping("/api/users/summary")
    public ResponseEntity<List<UserSummaryDto>> getSummaries(@RequestParam("ids") String idsCsv) {
        if (idsCsv == null || idsCsv.isBlank()) {
            return ResponseEntity.ok(List.of());
        }
        List<Long> ids = Arrays.stream(idsCsv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).collect(Collectors.toList());
        List<User> users = userRepository.findAllById(ids);
        List<UserSummaryDto> out = new ArrayList<>();
        for (User u : users) {
            // Determine display name: prefer actual name, fallback to username
            String displayName = (u.getName() != null && !u.getName().isBlank()) ? u.getName() : u.getUsername();
            UserSummaryDto dto = UserSummaryDto.builder()
                    .id(u.getId())
                    .displayName(displayName)
                    .username(u.getUsername())
                    .avatarUrl(u.getProfileImageUrl())
                    .verified(false)
                    .build();
            out.add(dto);
        }
        return ResponseEntity.ok(out);
    }
}
