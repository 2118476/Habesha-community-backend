package com.habesha.community.controller;

import com.habesha.community.dto.PaymentSessionRequest;
import com.habesha.community.model.Payment;
import com.habesha.community.model.User;
import com.habesha.community.service.PaymentService;
import com.habesha.community.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.List;
import java.util.Map;

/**
 * Endpoints for managing payments and Stripe sessions.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@Valid @RequestBody PaymentSessionRequest request) throws Exception {
        User payer = userService.getCurrentUser().orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return ResponseEntity.ok(paymentService.createCheckoutSession(request, payer));
    }

    /**
     * Endpoint for receiving Stripe webhook events.  This must be publicly
     * accessible and configured in the Stripe dashboard.  The raw body
     * and signature header are used to verify the event.  Stripe
     * recommends returning a 2xx response as quickly as possible.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(HttpServletRequest request) {
        try {
            StringBuilder payload = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
            String sigHeader = request.getHeader("Stripe-Signature");
            paymentService.handleWebhook(payload.toString(), sigHeader);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(400).body("Webhook error");
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<Payment>> paymentHistory() {
        User user = userService.getCurrentUser().orElseThrow(() -> new IllegalStateException("User not authenticated"));
        return ResponseEntity.ok(paymentService.getPaymentHistory(user));
    }
}