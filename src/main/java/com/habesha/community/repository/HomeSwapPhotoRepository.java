package com.habesha.community.repository;

import com.habesha.community.model.HomeSwapPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomeSwapPhotoRepository extends JpaRepository<HomeSwapPhoto, Long> {

    // Use nested property: homeSwap.id -> "homeSwap_Id"
    List<HomeSwapPhoto> findByHomeSwap_IdOrderBySortOrderAscIdAsc(Long homeSwapId);

    Optional<HomeSwapPhoto> findFirstByHomeSwap_IdOrderBySortOrderAscIdAsc(Long homeSwapId);

    void deleteByHomeSwap_Id(Long homeSwapId);

    boolean existsByHomeSwap_Id(Long homeSwapId);
}
