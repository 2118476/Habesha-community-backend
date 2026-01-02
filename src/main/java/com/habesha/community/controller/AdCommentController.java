package com.habesha.community.controller;

import com.habesha.community.dto.AdCommentDto;
import com.habesha.community.dto.AdCommentRequest;
import com.habesha.community.service.AdCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdCommentController {

    private final AdCommentService commentService;

    /* GET all comments (with replies) for one ad */
    @GetMapping("/api/ads/{adId}/comments")
    public ResponseEntity<List<AdCommentDto>> getCommentsForAd(
            @PathVariable Long adId
    ) {
        return ResponseEntity.ok(commentService.getThread(adId));
    }

    /* POST new top-level comment */
    @PostMapping("/api/ads/{adId}/comments")
    public ResponseEntity<AdCommentDto> createComment(
            @PathVariable Long adId,
            @Valid @RequestBody AdCommentRequest req
    ) {
        return ResponseEntity.ok(commentService.addComment(adId, req));
    }

    /* POST reply to an existing comment */
    @PostMapping("/api/ads/{adId}/comments/{parentId}/reply")
    public ResponseEntity<AdCommentDto> createReply(
            @PathVariable Long adId,
            @PathVariable Long parentId,
            @Valid @RequestBody AdCommentRequest req
    ) {
        return ResponseEntity.ok(commentService.addReply(adId, parentId, req));
    }

    /* PUT edit my comment */
    @PutMapping("/api/ad-comments/{commentId}")
    public ResponseEntity<AdCommentDto> editComment(
            @PathVariable Long commentId,
            @Valid @RequestBody AdCommentRequest req
    ) {
        return ResponseEntity.ok(commentService.editComment(commentId, req));
    }

    /* DELETE my comment */
    @DeleteMapping("/api/ad-comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
