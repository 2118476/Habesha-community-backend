// src/main/java/com/habesha/community/service/AdCommentService.java
package com.habesha.community.service;

import com.habesha.community.dto.AdCommentDto;
import com.habesha.community.dto.AdCommentRequest;
import com.habesha.community.model.AdComment;
import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.AdCommentRepository;
import com.habesha.community.repository.ClassifiedAdRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdCommentService {

    private final AdCommentRepository adCommentRepo;
    private final ClassifiedAdRepository adRepo;
    private final UserRepository userRepo;

    /** helper: current logged-in user */
    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Not authenticated");
        String email = auth.getName(); // You’re using email for username in UserDetails
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /** CREATE top-level comment */
    public AdCommentDto addComment(Long adId, AdCommentRequest req) {
        User author = getCurrentUserOrThrow();
        ClassifiedAd ad = adRepo.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        AdComment entity = AdComment.builder()
                .ad(ad)
                .author(author)
                .parent(null)
                .text(req.getText())
                .build();

        adCommentRepo.save(entity);
        return toDto(entity, author);
    }

    /** CREATE reply */
    public AdCommentDto addReply(Long adId, Long parentId, AdCommentRequest req) {
        User author = getCurrentUserOrThrow();
        ClassifiedAd ad = adRepo.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        AdComment parent = adCommentRepo.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        if (!parent.getAd().getId().equals(adId)) {
            throw new IllegalArgumentException("Parent does not belong to this ad");
        }

        AdComment reply = AdComment.builder()
                .ad(ad)
                .author(author)
                .parent(parent)
                .text(req.getText())
                .build();

        adCommentRepo.save(reply);
        return toDto(reply, author);
    }

    /** UPDATE my own comment */
    public AdCommentDto editComment(Long commentId, AdCommentRequest req) {
        User me = getCurrentUserOrThrow();
        AdComment c = adCommentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean isOwner = c.getAuthor().getId().equals(me.getId());
        boolean isAdmin = me.getRole() == Role.ADMIN || me.getRole() == Role.MODERATOR;

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not allowed to edit");
        }

        c.setText(req.getText());
        // @PreUpdate will set updatedAt
        return toDto(c, me);
    }

    /** DELETE my own comment */
    public void deleteComment(Long commentId) {
        User me = getCurrentUserOrThrow();
        AdComment c = adCommentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        boolean isOwner = c.getAuthor().getId().equals(me.getId());
        boolean isAdmin = me.getRole() == Role.ADMIN || me.getRole() == Role.MODERATOR;

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not allowed to delete");
        }

        adCommentRepo.delete(c);
    }

    /** READ thread (top-level comments + nested replies) */
    public List<AdCommentDto> getThread(Long adId) {
        User me = getCurrentUserOrThrow();

        List<AdComment> roots =
                adCommentRepo.findByAd_IdAndParentIsNullOrderByCreatedAtAsc(adId);

        return roots.stream()
                .map(root -> {
                    AdCommentDto dto = toDto(root, me);
                    List<AdComment> replies = adCommentRepo.findByParent_IdOrderByCreatedAtAsc(root.getId());
                    dto.setReplies(
                            replies.stream()
                                   .map(r -> toDto(r, me))
                                   .collect(Collectors.toList())
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /** map entity → dto (no replies here, caller fills replies) */
    private AdCommentDto toDto(AdComment c, User currentUser) {
        AdCommentDto dto = new AdCommentDto();
        dto.setId(c.getId());
        dto.setAuthorId(c.getAuthorId());
        dto.setAuthorName(c.getAuthorName());
        dto.setAuthorAvatar(c.getAuthorAvatar());
        dto.setText(c.getText());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());

        boolean isOwner = c.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MODERATOR;

        dto.setCanEdit(isOwner || isAdmin);
        dto.setCanDelete(isOwner || isAdmin);

        // replies filled separately
        dto.setReplies(null);

        return dto;
    }
    
}
