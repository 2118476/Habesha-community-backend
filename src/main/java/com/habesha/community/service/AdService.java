package com.habesha.community.service;

import com.habesha.community.dto.AdDetailsDto;
import com.habesha.community.dto.ClassifiedAdRequest;
import com.habesha.community.model.AdLike;
import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.AdCommentRepository;
import com.habesha.community.repository.AdLikeRepository;
import com.habesha.community.repository.ClassifiedAdRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for creating, reading, updating, deleting and reacting to
 * classified ads. Backs the Facebook-style AdDetails page.
 */
@Service
@RequiredArgsConstructor
public class AdService {

    private final ClassifiedAdRepository adRepository;
    private final UserRepository userRepository;
    private final AdLikeRepository adLikeRepository;
    private final AdCommentRepository adCommentRepository;

    /* ==========================================================
       AUTH HELPERS
       ----------------------------------------------------------
       getCurrentUser()        -> throws if not logged in
       getCurrentUserOrNull()  -> returns null if anonymous
       We use email as principal (UserDetails#getUsername() = email)
       ========================================================== */

    /**
     * Resolve the authenticated user from SecurityContext.
     * Throws if unauthenticated.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No current user");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No current user"));
    }

    /**
     * Same as getCurrentUser() but returns null if not logged in.
     * Useful for public GET endpoints where we still want to
     * know if THIS viewer already liked the ad.
     */
    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    /* ==========================================================
       CREATE
       ========================================================== */

    /**
     * Create a new ad owned by the logged-in user.
     */
    @Transactional
    public ClassifiedAd createAd(ClassifiedAdRequest request) {
        User poster = getCurrentUser();

        ClassifiedAd ad = ClassifiedAd.builder()
                .poster(poster)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .featured(request.isFeatured())
                .build();

        return adRepository.save(ad);
    }

    /* ==========================================================
       READ (LIST / DETAILS)
       ========================================================== */

    /**
     * Public list of ads (optionally filtered by category).
     * Used for marketplace feed / listings grid.
     * Returns ads ordered by creation date (newest first).
     */
    public List<ClassifiedAd> listAds(Optional<String> category) {
        return category
                .map(adRepository::findByCategoryIgnoreCaseOrderByCreatedAtDesc)
                .orElseGet(adRepository::findAllByOrderByCreatedAtDesc);
    }

    /**
     * Retrieve all ads created by a certain user.
     * Used for "My Ads" / profile listings.
     * Returns ads ordered by creation date (newest first).
     */
    public List<ClassifiedAd> listAdsByUser(Long userId) {
        return adRepository.findByPoster_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get a single ad WITH enriched social info for the frontend:
     *
     * - posterId, posterName, posterAvatar
     *   (so we can show avatar/name at the top like Facebook)
     *
     * - likeCount
     *   (long total number of likes on this ad)
     *
     * - likedByMe
     *   (true if the current viewer has liked it)
     *
     * This is what `/ads/{id}` returns to React.
     */
    public AdDetailsDto getAdDetails(Long id) {
        ClassifiedAd ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        // how many likes total?
        long likeCount = adLikeRepository.countByAd_Id(id);

        // does THIS viewer like it?
        User me = getCurrentUserOrNull();
        boolean likedByMe = false;
        if (me != null) {
            likedByMe = adLikeRepository.existsByAd_IdAndUser_Id(id, me.getId());
        }

        // poster info for header UI
        User poster = ad.getPoster();
        Long posterId = poster != null ? poster.getId() : null;

        // pick something nice for name
        String posterName = "Seller";
        if (poster != null) {
            if (poster.getName() != null && !poster.getName().isBlank()) {
                posterName = poster.getName();
            } else if (poster.getEmail() != null && !poster.getEmail().isBlank()) {
                posterName = poster.getEmail();
            }
        }

        // avatar URL (we already have getAvatarUrl() on User)
        String posterAvatar = (poster != null) ? poster.getAvatarUrl() : null;

        // Build DTO for frontend
        AdDetailsDto dto = new AdDetailsDto();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setImageUrl(ad.getImageUrl());
        dto.setCategory(ad.getCategory());
        dto.setFeatured(ad.isFeatured());
        dto.setCreatedAt(ad.getCreatedAt());

        dto.setPosterId(posterId);
        dto.setPosterName(posterName);
        dto.setPosterAvatar(posterAvatar);

        dto.setLikeCount(likeCount);
        dto.setLikedByMe(likedByMe);

        return dto;
    }

    /* ==========================================================
       UPDATE
       ========================================================== */

    /**
     * Edit an ad. Only the owner OR ADMIN can edit.
     */
    @Transactional
    public ClassifiedAd updateAd(Long id, ClassifiedAdRequest request) {
        User current = getCurrentUser();

        ClassifiedAd ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        boolean isOwner = ad.getPoster() != null
                && ad.getPoster().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not authorised to edit this ad");
        }

        ad.setTitle(request.getTitle());
        ad.setDescription(request.getDescription());
        ad.setPrice(request.getPrice());
        ad.setImageUrl(request.getImageUrl());
        ad.setCategory(request.getCategory());
        ad.setFeatured(request.isFeatured());

        return adRepository.save(ad);
    }

    /* ==========================================================
       DELETE
       ========================================================== */

    /**
     * Delete an ad. Only the owner OR ADMIN can delete.
     * Also deletes all associated likes and comments.
     */
    @Transactional
    public void deleteAd(Long id) {
        User current = getCurrentUser();

        ClassifiedAd ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        boolean isOwner = ad.getPoster() != null
                && ad.getPoster().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalStateException("Not authorised to delete this ad");
        }

        // Delete all associated data first (to avoid foreign key constraint violations)
        // 1. Delete all comments (including replies)
        adCommentRepository.deleteByAd_Id(id);
        
        // 2. Delete all likes
        adLikeRepository.deleteByAd_Id(id);

        // 3. Finally delete the ad itself
        adRepository.delete(ad);
    }

    /* ==========================================================
       LIKE / UNLIKE (Facebook-style reactions)
       ========================================================== */

    /**
     * Like this ad.
     * - Creates an AdLike row (ad_id + user_id).
     * - UniqueConstraint on (ad_id, user_id) prevents duplicates.
     * - Returns the new like count so frontend can update instantly.
     */
    @Transactional
    public long likeAd(Long adId) {
        User me = getCurrentUser();

        ClassifiedAd ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        boolean already = adLikeRepository.existsByAd_IdAndUser_Id(adId, me.getId());
        if (!already) {
            AdLike like = AdLike.builder()
                    .ad(ad)
                    .user(me)
                    .build(); // createdAt will be set in @PrePersist of AdLike
            adLikeRepository.save(like);
        }

        return adLikeRepository.countByAd_Id(adId);
    }

    /**
     * Unlike this ad.
     * - Removes that user's like row.
     * - Safe even if they hadn't liked it (idempotent).
     * - Returns the new like count.
     */
    @Transactional
    public long unlikeAd(Long adId) {
        User me = getCurrentUser();

        // force 404 if ad doesn't exist
        adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        adLikeRepository.deleteByAd_IdAndUser_Id(adId, me.getId());

        return adLikeRepository.countByAd_Id(adId);
    }

    /* ==========================================================
       UTILITY METHODS FOR PHOTO UPLOAD
       ========================================================== */

    /**
     * Get an ad by ID (for photo upload validation).
     */
    public ClassifiedAd getAdById(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
    }

    /**
     * Get an ad by ID with photos eagerly loaded.
     */
    public ClassifiedAd getAdByIdWithPhotos(Long id) {
        return adRepository.findByIdWithPhotos(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
    }

    /**
     * Save an ad (for updating imageUrl after photo upload).
     */
    @Transactional
    public ClassifiedAd saveAd(ClassifiedAd ad) {
        return adRepository.save(ad);
    }
    
    /**
     * Flush and clear the entity manager to ensure changes are committed.
     */
    @Transactional
    public void flushAndClear() {
        adRepository.flush();
    }
    
    /**
     * Update an ad's imageUrl in a separate transaction.
     */
    @Transactional
    public void updateAdImageUrl(Long adId, String imageUrl) {
        ClassifiedAd ad = getAdById(adId);
        System.out.println("Updating ad " + adId + " imageUrl from '" + ad.getImageUrl() + "' to '" + imageUrl + "'");
        ad.setImageUrl(imageUrl);
        ClassifiedAd saved = adRepository.save(ad);
        adRepository.flush(); // Force immediate persistence
        System.out.println("Ad imageUrl updated and flushed: " + saved.getImageUrl());
    }
}
