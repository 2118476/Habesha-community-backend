package com.habesha.community.controller;

import com.habesha.community.model.ClassifiedAd;
import com.habesha.community.model.User;
import com.habesha.community.service.AdService;
import com.habesha.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints scoped to the current authenticated user. These URIs
 * consistently live under the /api/my namespace and encapsulate
 * functionality that would otherwise be scattered across other
 * controllers. At present only ads are exposed; more resources can
 * be added here in future sprints.
 */
@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyContentController {

    private final UserService userService;
    private final AdService adService;

    /**
     * Return all classified advertisements posted by the current user.
     */
    @GetMapping("/ads")
    public ResponseEntity<List<ClassifiedAd>> myAds() {
        User u = userService.getCurrentUser().orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(adService.listAdsByUser(u.getId()));
    }
}