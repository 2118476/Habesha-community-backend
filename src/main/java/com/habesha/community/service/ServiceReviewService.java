package com.habesha.community.service;

import com.habesha.community.dto.ServiceReviewDto;
import com.habesha.community.dto.ServiceReviewSummaryDto;
import com.habesha.community.model.ServiceReview;
import com.habesha.community.model.User;
import com.habesha.community.repository.MessageRepository;
import com.habesha.community.repository.ServiceReviewRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

/**
 * Service reviews with anti-spam gating: a user may only review a provider
 * after a genuine two-way conversation (both sent >= MIN_MESSAGES_EACH messages).
 */
@Service
@RequiredArgsConstructor
public class ServiceReviewService {

    /** Both parties must have sent at least this many messages to each other. */
    private static final int MIN_MESSAGES_EACH = 3;

    private final ServiceReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public ServiceReviewSummaryDto getSummary(Long providerId) {
        userRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        List<ServiceReview> all = reviewRepository.findByProvider_IdOrderByCreatedAtDesc(providerId);
        Double avg = reviewRepository.averageRating(providerId);

        ServiceReviewDto myReview = null;
        boolean canReview = false;
        String reason = "Sign in to leave a review.";

        Optional<User> me = userService.getCurrentUser();
        if (me.isPresent()) {
            Long meId = me.get().getId();
            myReview = reviewRepository.findByProvider_IdAndReviewer_Id(providerId, meId)
                    .map(this::toDto).orElse(null);
            Eligibility elig = eligibility(meId, providerId);
            canReview = elig.allowed();
            reason = elig.reason();
        }

        return ServiceReviewSummaryDto.builder()
                .providerId(providerId)
                .average(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0)
                .count(all.size())
                .canReview(canReview)
                .reason(reason)
                .myReview(myReview)
                .reviews(all.stream().map(this::toDto).toList())
                .build();
    }

    @Transactional
    public ServiceReviewDto createOrUpdate(Long providerId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5.");
        }
        User me = userService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        Eligibility elig = eligibility(me.getId(), providerId);
        if (!elig.allowed()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, elig.reason());
        }

        ServiceReview review = reviewRepository.findByProvider_IdAndReviewer_Id(providerId, me.getId())
                .orElseGet(() -> ServiceReview.builder().provider(provider).reviewer(me).build());
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);
        return toDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteMine(Long providerId) {
        User me = userService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        reviewRepository.findByProvider_IdAndReviewer_Id(providerId, me.getId())
                .ifPresent(reviewRepository::delete);
    }

    /* -------------------- eligibility -------------------- */
    private record Eligibility(boolean allowed, String reason) {}

    private Eligibility eligibility(Long reviewerId, Long providerId) {
        if (reviewerId.equals(providerId)) {
            return new Eligibility(false, "You can’t review yourself.");
        }
        long sent = messageRepository.countBySender_IdAndRecipient_Id(reviewerId, providerId);
        long received = messageRepository.countBySender_IdAndRecipient_Id(providerId, reviewerId);
        if (sent >= MIN_MESSAGES_EACH && received >= MIN_MESSAGES_EACH) {
            return new Eligibility(true, "");
        }
        return new Eligibility(false,
                "You can review a provider only after a real conversation — you and they must each "
                        + "have sent at least " + MIN_MESSAGES_EACH + " messages.");
    }

    private ServiceReviewDto toDto(ServiceReview r) {
        return ServiceReviewDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .reviewer(userService.toSummary(r.getReviewer()))
                .build();
    }
}
