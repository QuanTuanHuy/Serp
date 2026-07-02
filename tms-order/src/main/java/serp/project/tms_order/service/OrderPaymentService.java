/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.InitiateOrderPaymentRequest;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.OrderPaymentInitResponse;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;

public interface OrderPaymentService {

    OrderPaymentInitResponse initiateOrderPayment(
            Long orderId,
            Long tenantId,
            InitiateOrderPaymentRequest request
    );

    OrderPaymentConfirmResponse confirmOrderPayment(
            Long orderId,
            Long tenantId,
            ConfirmOrderPaymentRequest request
    );

    PaymentWebhookProcessResponse processPaymentOrderConfirmedWebhook(PaymentOrderConfirmedWebhookRequest request);

    void updatePaymentStatus(String orderCode, Long tenantId, String paymentStatus);
}
