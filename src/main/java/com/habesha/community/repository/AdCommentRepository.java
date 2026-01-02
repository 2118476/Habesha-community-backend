package com.habesha.community.repository;

import com.habesha.community.model.AdComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdCommentRepository extends JpaRepository<AdComment, Long> {

    // Existing
    List<AdComment> findByAd_IdAndParentIsNullOrderByCreatedAtAsc(Long adId);
    List<AdComment> findByParent_IdOrderByCreatedAtAsc(Long parentId);

    // delete all comments for an ad (used when deleting the ad)
    void deleteByAd_Id(Long adId);

    // === NEW for activity feed & unread counts ===
    List<AdComment> findTop50ByAd_Poster_IdOrderByCreatedAtDesc(Long posterId);

    long countByAd_Poster_IdAndCreatedAtAfter(Long posterId, LocalDateTime after);
}
