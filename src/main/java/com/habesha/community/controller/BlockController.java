package com.habesha.community.controller;

import com.habesha.community.service.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    /** List my blocked users. */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        List<Map<String, Object>> items = blockService.listMyBlocks().stream()
            .map(b -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", b.id());
                m.put("blockedAt", b.blockedAt());
                m.put("userId", b.user().getId());
                m.put("username", b.user().getUsername());
                m.put("displayName", b.user().getDisplayName());
                m.put("avatarUrl", b.user().getAvatarUrl());
                return m;
            })
            .toList();

        return ResponseEntity.ok(items);
    }

    /** Block target user by id. */
    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> block(@PathVariable Long targetUserId) {
        blockService.blockUser(targetUserId);
        return ResponseEntity.noContent().build();
    }

    /** Unblock by block record id. */
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> unblock(@PathVariable Long blockId) {
        blockService.unblock(blockId);
        return ResponseEntity.noContent().build();
    }
}
