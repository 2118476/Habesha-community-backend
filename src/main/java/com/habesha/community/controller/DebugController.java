package com.habesha.community.controller;

import com.habesha.community.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple debugging endpoints that should never be exposed in a
 * production environment. These are intended for local development
 * troubleshooting. The {@code /debug/cloudinary} endpoint returns
 * the presence (not the values) of Cloudinary related environment
 * variables so that developers can verify their configuration without
 * exposing secrets.
 */
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final MailService mailService;

    /**
     * Inspect Cloudinary environment variables. Only indicates whether
     * each variable is defined. Does not include the raw secret.
     *
     * @return a map of variable names to booleans indicating presence
     */
    @GetMapping("/cloudinary")
    public Map<String, Object> cloudinaryVars() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("CLOUDINARY_URL", System.getenv("CLOUDINARY_URL") != null);
        vars.put("CLOUDINARY_CLOUD_NAME", System.getenv("CLOUDINARY_CLOUD_NAME") != null);
        vars.put("CLOUDINARY_API_KEY", System.getenv("CLOUDINARY_API_KEY") != null);
        vars.put("CLOUDINARY_API_SECRET", System.getenv("CLOUDINARY_API_SECRET") != null);
        return vars;
    }

    /**
     * Test email functionality by sending a test email.
     * This endpoint should be removed after confirming email works.
     *
     * @param to the email address to send the test email to
     * @return success or error message
     */
    @GetMapping("/mail-test")
    public ResponseEntity<Map<String, String>> testMail(@RequestParam String to) {
        try {
            mailService.sendTestEmail(to);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test email sent successfully to " + to);
            log.info("Test email sent successfully to: {}", to);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to send test email: " + e.getMessage());
            log.error("Failed to send test email to: {}", to, e);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}