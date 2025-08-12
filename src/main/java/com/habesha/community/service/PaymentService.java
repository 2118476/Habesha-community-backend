package com.habesha.community.service;

import com.habesha.community.dto.PaymentSessionRequest;
import com.habesha.community.model.*;
import com.habesha.community.repository.PaymentRepository;
import com.habesha.community.repository.ServiceBookingRepository;
import com.habesha.community.repository.ServiceOfferRepository;
import com.habesha.community.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Integrates with Stripe to create checkout sessions and process
 * webhook events.  For simplicity this implementation assumes
 * payments are for a single line item corresponding to a service,
 * event or ad.  Once payment succeeds a corresponding payment record
 * is stored in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    @Value("${stripe.secretKey}")
    private String stripeSecretKey;
    @Value("${stripe.webhookSecret}")
    private String stripeWebhookSecret;

    private final PaymentRepository paymentRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final ServiceBookingRepository bookingRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        // Stripe API key is set globally via static setter
        com.stripe.Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Creates a Stripe Checkout session for the given request.  The
     * line item price is determined by the target entity.  The
     * success and cancel URLs are provided by the frontend.
     */
    @Transactional
    public Map<String, String> createCheckoutSession(PaymentSessionRequest request, User payer) throws Exception {
        BigDecimal amount;
        String description;
        // Determine the price based on the type
        if (request.getType() == PaymentType.SERVICE) {
            ServiceOffer offer = serviceOfferRepository.findById(request.getTargetId()).orElseThrow(() -> new IllegalArgumentException("Service not found"));
            // Commission logic resides in booking service; here we just use base price
            amount = offer.getBasePrice();
            description = "Booking for " + offer.getTitle();
        } else {
            // For other types, the price is fixed (e.g. £10 for featuring) – this could be configured via admin
            amount = BigDecimal.valueOf(10);
            description = request.getType().name() + " promotion";
        }
        // Convert to smallest currency unit (e.g. pence)
        long unitAmount = amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.UP).longValue();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(request.getCancelUrl())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("gbp")
                                .setUnitAmount(unitAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(description)
                                        .build())
                                .build())
                        .build())
                .putMetadata("type", request.getType().name())
                .putMetadata("targetId", request.getTargetId().toString())
                .putMetadata("payerId", payer.getId().toString())
                .build();
        Session session = Session.create(params);
        // Persist a pending payment record
        Payment payment = Payment.builder()
                .payer(payer)
                .amount(amount)
                .currency("GBP")
                .reference(session.getId())
                .description(description)
                .type(request.getType())
                .targetId(request.getTargetId())
                .status(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);
        return Map.of("url", session.getUrl());
    }

    /**
     * Handles Stripe webhook events.  Validates the signature and
     * updates payment records accordingly.
     */
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            return;
        }
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String sessionId = session.getId();
                paymentRepository.findAll().stream()
                        .filter(p -> sessionId.equals(p.getReference()))
                        .findFirst()
                        .ifPresent(p -> {
                            p.setStatus(PaymentStatus.SUCCEEDED);
                            paymentRepository.save(p);
                        });
            }
        }
    }

    public List<Payment> getPaymentHistory(User user) {
        return paymentRepository.findByPayer(user);
    }
}