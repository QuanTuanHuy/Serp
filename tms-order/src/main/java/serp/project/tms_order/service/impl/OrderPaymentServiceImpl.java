/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.caller.PaymentServiceCaller;
import serp.project.tms_order.caller.dto.payment.PaymentCreateOrderRequest;
import serp.project.tms_order.caller.dto.payment.PaymentCreateOrderResponse;
import serp.project.tms_order.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.InitiateOrderPaymentRequest;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.OrderPaymentInitResponse;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.OrderEventDispatcher;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.OrderPaymentService;
import serp.project.tms_order.service.order.OrderAccessPolicy;
import serp.project.tms_order.service.order.OrderTextUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentServiceCaller paymentServiceCaller;
    private final OrderAccessPolicy orderAccessPolicy;
    private final OrderEventDispatcher orderEventDispatcher;

    @Value("${payment.service.redirect-url:http://localhost:3000/payment/result}")
    private String paymentRedirectUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentInitResponse initiateOrderPayment(
            Long orderId,
            Long tenantId,
            InitiateOrderPaymentRequest request
    ) {
        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee is already paid.");
        }

        long shippingFee = resolveShippingFeeForPayment(order, request);
        order.setTotalShippingFee(shippingFee);
        orderRepository.save(order);

        Long actorId = orderAccessPolicy.getCurrentUserIdOrThrow();
        String orderCode = order.getOrderCode();
        PaymentCreateOrderRequest paymentRequest = PaymentCreateOrderRequest.builder()
                .appUser(orderCode)
                .amount(shippingFee)
                .description("Thanh toan phi van chuyen cho don hang " + orderCode)
                .embedData(PaymentCreateOrderRequest.EmbedData.builder()
                        .redirectUrl(paymentRedirectUrl + "?source=tms-order&orderId=" + order.getId())
                        .build())
                .title("Phi van chuyen - " + orderCode)
                .tenantId(tenantId)
                .actorId(actorId)
                .userId(actorId)
                .items(List.of(PaymentCreateOrderRequest.Item.builder()
                        .itemId("shipping-fee-" + orderCode)
                        .itemName("Phi van chuyen don hang " + orderCode)
                        .itemPrice(shippingFee)
                        .itemQuantity(1)
                        .build()))
                .build();

        PaymentCreateOrderResponse paymentResponse = paymentServiceCaller.createOrder(paymentRequest);
        if (paymentResponse.getStatus() == null || !"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    paymentResponse.getMessage() == null
                            ? "Cannot create payment order for shipping fee."
                            : paymentResponse.getMessage()
            );
        }

        return new OrderPaymentInitResponse(
                order.getId(),
                orderCode,
                shippingFee,
                paymentResponse.getAppTransId(),
                paymentResponse.getOrderUrl(),
                paymentResponse.getStatus(),
                paymentResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderPaymentConfirmResponse confirmOrderPayment(
            Long orderId,
            Long tenantId,
            ConfirmOrderPaymentRequest request
    ) {
        if (request == null || !OrderTextUtils.hasText(request.getAppTransId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Order order = orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        orderAccessPolicy.validateCanMutateOrder(order);

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return new OrderPaymentConfirmResponse(
                    order.getId(),
                    order.getOrderCode(),
                    order.getPaymentStatus(),
                    request.getAppTransId(),
                    "SUCCESS",
                    "Order shipping fee is already marked as paid."
            );
        }

        PaymentQueryOrderResponse queryResponse = paymentServiceCaller.queryOrderStatus(request.getAppTransId());
        String gatewayStatus = queryResponse.getStatus() == null ? "UNKNOWN" : queryResponse.getStatus();
        if (!"SUCCESS".equalsIgnoreCase(gatewayStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Payment status is not successful yet. Current status: " + gatewayStatus
            );
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        Order savedOrder = orderRepository.save(order);
        orderEventDispatcher.publishOrderAfterCommit(savedOrder);
        orderEventDispatcher.publishOrderPaymentSucceededNotificationAfterCommit(savedOrder);

        return new OrderPaymentConfirmResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getPaymentStatus(),
                request.getAppTransId(),
                gatewayStatus,
                queryResponse.getMessage()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentWebhookProcessResponse processPaymentOrderConfirmedWebhook(
            PaymentOrderConfirmedWebhookRequest request
    ) {
        if (request == null || !OrderTextUtils.hasText(request.getOrderCode()) || request.getTenantId() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Invalid webhook payload.");
        }

        String orderCode = request.getOrderCode().trim();
        Order order = orderRepository.findByOrderCodeAndTenantIdForUpdate(orderCode, request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (PaymentStatus.PAID.equals(order.getPaymentStatus())) {
            return new PaymentWebhookProcessResponse(
                    orderCode,
                    request.getAppTransId(),
                    false,
                    "Order shipping fee is already marked as paid."
            );
        }

        if (request.getAmount() != null && request.getAmount() > 0L) {
            order.setTotalShippingFee(request.getAmount());
        }
        order.setPaymentStatus(PaymentStatus.PAID);

        Order savedOrder = orderRepository.save(order);
        orderEventDispatcher.publishOrderAfterCommit(savedOrder);
        orderEventDispatcher.publishOrderPaymentSucceededNotificationAfterCommit(savedOrder);

        return new PaymentWebhookProcessResponse(
                savedOrder.getOrderCode(),
                request.getAppTransId(),
                true,
                "Order payment status updated to PAID."
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(String orderCode, Long tenantId, String paymentStatus) {
        Order order = orderRepository.findByOrderCodeAndTenantId(orderCode, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        PaymentStatus status = PaymentStatus.valueOf(paymentStatus);
        PaymentStatus currentStatus = order.getPaymentStatus();
        order.setPaymentStatus(status);
        Order savedOrder = orderRepository.save(order);
        if (!PaymentStatus.PAID.equals(currentStatus) && PaymentStatus.PAID.equals(status)) {
            orderEventDispatcher.publishOrderPaymentSucceededNotificationAfterCommit(savedOrder);
        }
        log.info("Updated payment status for order {} to {} (tenant {})", orderCode, paymentStatus, tenantId);
    }

    private long resolveShippingFeeForPayment(Order order, InitiateOrderPaymentRequest request) {
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (request != null && request.getAmount() != null) {
            Long amount = request.getAmount();
            if (amount <= 0L) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee must be greater than 0 for payment.");
            }
            return amount;
        }
        Long totalShippingFee = order.getTotalShippingFee();
        if (totalShippingFee == null || totalShippingFee <= 0L) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Order shipping fee must be greater than 0 for payment.");
        }
        return totalShippingFee;
    }
}
