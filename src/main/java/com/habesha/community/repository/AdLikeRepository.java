package com.habesha.community.repository;

import com.habesha.community.model.AdLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AdLikeRepository extends JpaRepository<AdLike, Long> {

    // how many likes for this ad
    long countByAd_Id(Long adId);

    // has this user already liked this ad?
    boolean existsByAd_IdAndUser_Id(Long adId, Long userId);

    // unlike
    void deleteByAd_IdAndUser_Id(Long adId, Long userId);

    // delete all likes for an ad (used when deleting the ad)
    void deleteByAd_Id(Long adId);

    // === NEW for activity feed & unread counts ===
    List<AdLike> findTop50ByAd_Poster_IdOrderByCreatedAtDesc(Long posterId);

    long countByAd_Poster_IdAndCreatedAtAfter(Long posterId, LocalDateTime after);
}
