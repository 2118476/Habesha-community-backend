package com.habesha.community.service;

import com.habesha.community.dto.ClassifiedAdRequest;
import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
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
 * Service for creating and managing classified adverts.
 */
@Service
@RequiredArgsConstructor
public class AdService {
    private final ClassifiedAdRepository adRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName()).orElseThrow(() -> new IllegalStateException("No current user"));
    }

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

    public List<ClassifiedAd> listAds(Optional<String> category) {
        return category.map(adRepository::findByCategoryIgnoreCase).orElseGet(adRepository::findAll);
    }

    @Transactional
    public void deleteAd(Long id) {
        User current = getCurrentUser();
        ClassifiedAd ad = adRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ad not found"));
        if (!ad.getPoster().getId().equals(current.getId()) && current.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorised to delete this ad");
        }
        adRepository.delete(ad);
    }
}