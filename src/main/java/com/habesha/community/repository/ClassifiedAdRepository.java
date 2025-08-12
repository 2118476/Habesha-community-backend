package com.habesha.community.repository;

import com.habesha.community.model.ClassifiedAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassifiedAdRepository extends JpaRepository<ClassifiedAd, Long> {
    List<ClassifiedAd> findByCategoryIgnoreCase(String category);
}