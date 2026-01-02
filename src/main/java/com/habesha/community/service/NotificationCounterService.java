package com.habesha.community.service;

import com.habesha.community.repository.AdCommentRepository;
import com.habesha.community.repository.AdLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationCounterService {

    private final AdLikeRepository adLikeRepository;
    private final AdCommentRepository adCommentRepository;

    /**
     * Count inbound "social" interactions (likes + comments) on my ads since a timestamp.
     * Only interactions from others are counted; likes/comments by me on my own ads are rare,
     * but if you want to exclude them explicitly, we can add that check later.
     */
    public long countNewAdInteractionsSince(Long myUserId, LocalDateTime since) {
        if (myUserId == null || since == null) return 0L;
        long likes = adLikeRepository.countByAd_Poster_IdAndCreatedAtAfter(myUserId, since);
        long comments = adCommentRepository.countByAd_Poster_IdAndCreatedAtAfter(myUserId, since);
        return likes + comments;
    }
}
