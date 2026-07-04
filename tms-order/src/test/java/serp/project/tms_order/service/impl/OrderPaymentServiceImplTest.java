/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.tms_order.caller.PaymentServiceCaller;
import serp.project.tms_order.caller.dto.payment.PaymentQueryOrderResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.kafka.OrderEventDispatcher;
import serp.project.tms_order.kafka.OrderNotificationEventPublisher;
import serp.project.tms_order.kafka.OrderSyncEventPublisher;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.order.OrderAccessPolicy;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceImplTest {

    private static final Long TENANT_ID = 9L;
    private static final Long CUSTOMER_USER_ID = 42L;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentServiceCaller paymentServiceCaller;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private OrderSyncEventPublisher orderSyncEventPublisher;

    @Mock
    private OrderNotificationEventPublisher orderNotificationEventPublisher;

    private OrderPaymentServiceImpl orderPaymentService;

    @BeforeEach
    void setUp() {
        OrderEventDispatcher orderEventDispatcher = new OrderEventDispatcher(
                orderSyncEventPublisher,
                orderNotificationEventPublisher
        );
        orderPaymentService = new OrderPaymentServiceImpl(
                orderRepository,
                paymentServiceCaller,
                new OrderAccessPolicy(authUtils),
                orderEventDispatcher
        );
    }

    @Test
    void confirmOrderPaymentReturnsSuccessWhenOrderAlreadyPaid() {
        Order order = confirmableOrder();
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);

        OrderPaymentConfirmResponse response = orderPaymentService.confirmOrderPayment(
                1L,
                TENANT_ID,
                new ConfirmOrderPaymentRequest("250101_abc")
        );

        assertEquals(PaymentStatus.PAID, response.paymentStatus());
        assertEquals("SUCCESS", response.gatewayStatus());
        verify(paymentServiceCaller, never()).queryOrderStatus(any());
        verify(orderSyncEventPublisher, never()).publish(any());
        verify(orderNotificationEventPublisher, never()).publishOrderPaymentSucceeded(any());
    }

    @Test
    void confirmOrderPaymentMarksOrderPaidAndPublishesNotification() {
        Order order = confirmableOrder();
        order.setPaymentStatus(PaymentStatus.UNPAID);

        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);
        when(paymentServiceCaller.queryOrderStatus("250101_abc"))
                .thenReturn(PaymentQueryOrderResponse.builder()
                        .status("SUCCESS")
                        .message("Payment completed")
                        .build());
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderPaymentConfirmResponse response = orderPaymentService.confirmOrderPayment(
                1L,
                TENANT_ID,
                new ConfirmOrderPaymentRequest("250101_abc")
        );

        assertEquals(PaymentStatus.PAID, response.paymentStatus());
        verify(orderSyncEventPublisher).publish(order);
        verify(orderNotificationEventPublisher).publishOrderPaymentSucceeded(order);
    }

    @Test
    void processPaymentWebhookMarksOrderPaidAndPublishesSyncEvent() {
        Order order = confirmableOrder();
        order.setPaymentStatus(PaymentStatus.UNPAID);

        when(orderRepository.findByOrderCodeAndTenantIdForUpdate("ORD-001", TENANT_ID))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentWebhookProcessResponse response = orderPaymentService.processPaymentOrderConfirmedWebhook(
                new PaymentOrderConfirmedWebhookRequest(
                        "250101_abc",
                        "ORD-001",
                        TENANT_ID,
                        45000L,
                        null,
                        "zalopay",
                        "gateway-1"
                )
        );

        assertTrue(response.updated());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(45000L, order.getTotalShippingFee());
        verify(orderSyncEventPublisher).publish(order);
        verify(orderNotificationEventPublisher).publishOrderPaymentSucceeded(order);
    }

    private Order confirmableOrder() {
        return Order.builder()
                .id(1L)
                .orderCode("ORD-001")
                .customerOrderCode("CUS-001")
                .status(OrderStatus.CREATED)
                .isConfirm(false)
                .pickupMethod(OrderPickupMethod.COURIER_PICKUP)
                .feePayer(FeePayer.RECEIVER)
                .paymentStatus(PaymentStatus.UNPAID)
                .senderLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(105.8342D, 21.0278D)))
                .receiverLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(106.7009D, 10.7769D)))
                .tenantId(TENANT_ID)
                .createdBy(String.valueOf(CUSTOMER_USER_ID))
                .build();
    }
}
