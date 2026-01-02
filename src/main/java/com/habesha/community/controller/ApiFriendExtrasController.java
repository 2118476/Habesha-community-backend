package com.habesha.community.controller;

import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adds small extras that weren't present in ApiFriendController,
 * without touching your existing mappings.
 */
@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class ApiFriendExtrasController {

    private final FriendService friendService;

    /**
     * GET /api/friends/mutual/{targetUserId}
     * Returns mutual friends between the authenticated user and the target.
     */
    @GetMapping("/mutual/{targetUserId}")
    public List<UserSummaryDto> mutual(@PathVariable Long targetUserId) {
        return friendService.getMutualFriends(targetUserId);
    }
}
