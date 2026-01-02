package com.habesha.community.controller;

import com.habesha.community.service.BlockService;
import com.habesha.community.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RelationshipController {

    private final BlockService blockService;
    private final FriendService friendService;

    @GetMapping("/friends/status")
    public ResponseEntity<?> friendshipStatus(@RequestParam("targetId") Long targetId) {
        try {
            // Check if blocked first
            boolean isBlocked = blockService.isBlockedByOrBlocking(targetId);
            if (isBlocked) {
                return ResponseEntity.ok(Map.of(
                    "isFriend", false,
                    "isMutualFriend", false,
                    "isBlocked", true
                ));
            }
            
            // Check friendship status
            boolean isFriend = friendService.areFriends(targetId);
            return ResponseEntity.ok(Map.of(
                "isFriend", isFriend,
                "isMutualFriend", isFriend,
                "isBlocked", false
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "isFriend", false,
                "isMutualFriend", false,
                "isBlocked", false
            ));
        }
    }

    @PostMapping("/contact-requests")
    public ResponseEntity<?> requestContact(@RequestBody Map<String, Object> body) {
        // This is now handled by ContactController
        return ResponseEntity.ok(Map.of("ok", true));
    }
    
    @GetMapping("/relationship/check")
    public ResponseEntity<?> checkRelationship(@RequestParam("targetId") Long targetId) {
        try {
            boolean isBlocked = blockService.isBlockedByOrBlocking(targetId);
            boolean isFriend = !isBlocked && friendService.areFriends(targetId);
            
            return ResponseEntity.ok(Map.of(
                "isBlocked", isBlocked,
                "isFriend", isFriend,
                "canView", !isBlocked
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "isBlocked", false,
                "isFriend", false,
                "canView", true
            ));
        }
    }
}
