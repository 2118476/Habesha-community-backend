package com.habesha.community.repository;

import com.habesha.community.model.AdPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdPhotoRepository extends JpaRepository<AdPhoto, Long> {
    List<AdPhoto> findByAd_IdOrderBySortIndexAsc(Long adId);
    void deleteByAd_Id(Long adId);
}