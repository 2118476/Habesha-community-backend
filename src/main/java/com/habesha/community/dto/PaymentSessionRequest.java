package com.habesha.community.dto;

import com.habesha.community.model.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Payload for requesting a new Stripe checkout session.  The
 * frontend should supply the type and target ID so the backend can
 * populate line items accordingly.  Success and cancel URLs point
 * back to the frontend to handle the result.
 */
@Data
public class PaymentSessionRequest {
    @NotNull
    private PaymentType type;
    @NotNull
    private Long targetId;
    @NotBlank
    private String successUrl;
    @NotBlank
    private String cancelUrl;
}