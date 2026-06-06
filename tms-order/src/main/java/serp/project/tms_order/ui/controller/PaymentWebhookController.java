/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.service.OrderService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/internal/payment-webhooks")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private static final String WEBHOOK_SECRET_HEADER = "X-Webhook-Secret";

    @Value("${payment.webhook.secret:change-me}")
    private String paymentWebhookSecret;

    private final OrderService orderService;

    @PostMapping("/orders/payment-confirmed")
    public PaymentWebhookProcessResponse handleOrderPaymentConfirmedWebhook(
            @RequestHeader(value = WEBHOOK_SECRET_HEADER, required = false) String webhookSecret,
            @Valid @RequestBody PaymentOrderConfirmedWebhookRequest request
    ) {
        validateWebhookSecret(webhookSecret);
        log.info(
                "Received payment confirmed webhook appTransId={} orderCode={} tenantId={}",
                request.getAppTransId(),
                request.getOrderCode(),
                request.getTenantId()
        );
        return orderService.processPaymentOrderConfirmedWebhook(request);
    }

    private void validateWebhookSecret(String providedSecret) {
        if (paymentWebhookSecret == null || paymentWebhookSecret.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Payment webhook secret is not configured.");
        }
        if (providedSecret == null || providedSecret.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Missing payment webhook secret.");
        }

        byte[] expected = paymentWebhookSecret.trim().getBytes(StandardCharsets.UTF_8);
        byte[] actual = providedSecret.trim().getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Invalid payment webhook secret.");
        }
    }
}
