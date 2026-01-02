package com.habesha.community.service;

import com.habesha.community.dto.UserSummaryDto;
import com.habesha.community.model.User;
import com.habesha.community.model.UserBlock;
import com.habesha.community.repository.UserBlockRepository;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {
    private final UserBlockRepository blockRepo;
    private final UserRepository userRepo;

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;
        if (principal instanceof User u) return u;
        if (principal instanceof org.springframework.security.core.userdetails.User du) {
            return userRepo.findByUsername(du.getUsername()).orElseThrow();
        }
        throw new IllegalStateException("Unauthenticated");
    }

    public List<BlockListItem> listMyBlocks() {
        var me = currentUser();
        return blockRepo.findByBlocker_Id(me.getId())
                .stream()
                .map(b -> new BlockListItem(
                        b.getId(),
                        b.getCreatedAt(),
                        toSummary(b.getBlocked())
                ))
                .collect(Collectors.toList());
    }

    public void blockUser(Long targetId) {
        var me = currentUser();
        var target = userRepo.findById(targetId).orElseThrow();
        if (blockRepo.existsByBlocker_IdAndBlocked_Id(me.getId(), targetId)) {
            return; // idempotent
        }
        var rec = UserBlock.builder()
                .blocker(me)
                .blocked(target)
                .createdAt(LocalDateTime.now())
                .build();
        blockRepo.save(rec);
    }

    public void unblock(Long blockId) {
        var me = currentUser();
        var rec = blockRepo.findByIdAndBlocker_Id(blockId, me.getId())
                .orElseThrow(() -> new IllegalArgumentException("Block not found"));
        blockRepo.delete(rec);
    }
    
    /**
     * Check if user A has blocked user B or vice versa.
     * Returns true if either user has blocked the other.
     */
    public boolean isBlocked(Long userAId, Long userBId) {
        return blockRepo.existsBidirectionalBlock(userAId, userBId);
    }
    
    /**
     * Check if the current user has blocked or been blocked by the target user.
     */
    public boolean isBlockedByOrBlocking(Long targetUserId) {
        var me = currentUser();
        return isBlocked(me.getId(), targetUserId);
    }

    private UserSummaryDto toSummary(User u) {
        return UserSummaryDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .displayName(u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getUsername())
                .avatarUrl(u.getAvatarUrl())
                .verified(false)
                .build();
    }

    /** Lightweight DTO for the blocked list. */
    public record BlockListItem(Long id, java.time.LocalDateTime blockedAt, UserSummaryDto user) {}
}
