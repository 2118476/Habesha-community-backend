package com.habesha.community.controller;

import com.habesha.community.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Miscellaneous endpoints including the terms and conditions and
 * account deletion to support GDPR compliance.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class GeneralController {
    private final UserService userService;

    /**
     * Returns a placeholder terms and conditions text.  The actual
     * contents should be provided by the legal team and can be
     * externalised into a file or database.
     */
    @GetMapping("/terms")
    public ResponseEntity<String> terms() {
        String disclaimer = "Use at your own risk. The platform provides a way to connect " +
                "members of the Ethiopian diaspora but does not vet users or guarantee " +
                "the accuracy of information. Always exercise caution when meeting " +
                "others or transacting online.";
        return ResponseEntity.ok(disclaimer);
    }

    /**
     * Allows the currently authenticated user to permanently delete
     * their account and associated data.  This operation cannot be
     * undone and is provided for GDPR compliance.
     */
    @DeleteMapping("/account")
    @Transactional
    public ResponseEntity<Void> deleteAccount() {
        userService.getCurrentUser().ifPresent(user -> userService.deleteUser(user.getId()));
        return ResponseEntity.noContent().build();
    }
}