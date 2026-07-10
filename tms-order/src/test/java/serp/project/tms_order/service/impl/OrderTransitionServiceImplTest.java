/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.kafka.OrderNotificationEventPublisher;
import serp.project.tms_order.kafka.OrderSyncEventPublisher;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.OrderTransitionLogRepository;
import serp.project.tms_order.service.OrderTimelineService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderTransitionServiceImplTest {
    private static final Long TENANT_ID = 1L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTransitionLogRepository transitionLogRepository;

    @Mock
    private OrderTimelineService orderTimelineService;

    @Mock
    private OrderSyncEventPublisher orderSyncEventPublisher;

    @Mock
    private OrderNotificationEventPublisher orderNotificationEventPublisher;

    private OrderTransitionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderTransitionServiceImpl(
                new ObjectMapper(),
                orderRepository,
                transitionLogRepository,
                orderTimelineService,
                orderSyncEventPublisher,
                orderNotificationEventPublisher
        );
    }

    @Test
    void appliesBagDistributionLifecycleStatuses() {
        Order order = Order.builder()
                .id(10L)
                .orderCode("ORD-001")
                .status(OrderStatus.BAG_SEALED)
                .tenantId(TENANT_ID)
                .build();
        when(transitionLogRepository.findByTenantIdAndIdempotencyKey(TENANT_ID, "bag-distribution:test"))
                .thenReturn(Optional.empty());
        when(orderRepository.findByIdAndTenantIdForUpdate(10L, TENANT_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyTransitions(transitionRequest(
                "bag-distribution:test",
                OrderStatus.BAG_SEALED,
                OrderStatus.BAG_IN_TRANSIT
        ), TENANT_ID);

        assertEquals(OrderStatus.BAG_IN_TRANSIT, order.getStatus());

        service.applyTransitions(transitionRequest(
                "bag-distribution:test:inbound",
                OrderStatus.BAG_IN_TRANSIT,
                OrderStatus.INBOUND_AT_DESTINATION_HUB
        ), TENANT_ID);

        assertEquals(OrderStatus.INBOUND_AT_DESTINATION_HUB, order.getStatus());
        assertEquals(11L, order.getCurrentHubId());
        assertEquals("HUB-11", order.getCurrentHubCode());
    }

    @Test
    void appliesBagDistributionAcrossMultipleHubsThenDestinationPostOffice() {
        Order order = Order.builder()
                .id(10L)
                .orderCode("ORD-001")
                .status(OrderStatus.INBOUND_AT_DESTINATION_HUB)
                .tenantId(TENANT_ID)
                .currentHubId(11L)
                .currentHubCode("HUB-11")
                .build();
        when(transitionLogRepository.findByTenantIdAndIdempotencyKey(any(), any()))
                .thenReturn(Optional.empty());
        when(orderRepository.findByIdAndTenantIdForUpdate(10L, TENANT_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyTransitions(transitionRequest(
                "bag-distribution:test:outbound-hub-11",
                List.of(OrderStatus.BAG_SEALED, OrderStatus.INBOUND_AT_DESTINATION_HUB, OrderStatus.BAG_IN_TRANSIT),
                OrderStatus.BAG_IN_TRANSIT,
                11L,
                "HUB-11"
        ), TENANT_ID);

        assertEquals(OrderStatus.BAG_IN_TRANSIT, order.getStatus());

        service.applyTransitions(transitionRequest(
                "bag-distribution:test:inbound-hub-12",
                List.of(OrderStatus.BAG_IN_TRANSIT, OrderStatus.INBOUND_AT_DESTINATION_HUB),
                OrderStatus.INBOUND_AT_DESTINATION_HUB,
                12L,
                "HUB-12"
        ), TENANT_ID);

        assertEquals(OrderStatus.INBOUND_AT_DESTINATION_HUB, order.getStatus());
        assertEquals(12L, order.getCurrentHubId());
        assertEquals("HUB-12", order.getCurrentHubCode());

        service.applyTransitions(transitionRequest(
                "bag-distribution:test:outbound-hub-12",
                List.of(OrderStatus.BAG_SEALED, OrderStatus.INBOUND_AT_DESTINATION_HUB, OrderStatus.BAG_IN_TRANSIT),
                OrderStatus.BAG_IN_TRANSIT,
                12L,
                "HUB-12"
        ), TENANT_ID);

        assertEquals(OrderStatus.BAG_IN_TRANSIT, order.getStatus());

        service.applyTransitions(transitionRequest(
                "bag-distribution:test:inbound-post-office",
                List.of(OrderStatus.BAG_IN_TRANSIT, OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE),
                OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE,
                12L,
                "HUB-12"
        ), TENANT_ID);

        assertEquals(OrderStatus.INBOUND_AT_DESTINATION_POST_OFFICE, order.getStatus());
        assertNull(order.getCurrentHubId());
        assertNull(order.getCurrentHubCode());
    }

    @Test
    void allowsDeliveryCheckinToDeliverReadyForDeliveryOrder() {
        Order order = Order.builder()
                .id(10L)
                .orderCode("ORD-001")
                .status(OrderStatus.READY_FOR_DELIVERY)
                .tenantId(TENANT_ID)
                .build();
        when(transitionLogRepository.findByTenantIdAndIdempotencyKey(TENANT_ID, "last-mile:delivered"))
                .thenReturn(Optional.empty());
        when(orderRepository.findByIdAndTenantIdForUpdate(10L, TENANT_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.applyTransitions(transitionRequest(
                "last-mile:delivered",
                List.of(OrderStatus.READY_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY),
                OrderStatus.DELIVERED,
                null,
                null
        ), TENANT_ID);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        verify(orderNotificationEventPublisher).publishOrderStatusTransition(
                eq(order),
                eq(OrderStatus.DELIVERED),
                any()
        );
    }

    @Test
    void recordsTimelineWhenTargetStatusAlreadyAppliedAndRequested() {
        Order order = Order.builder()
                .id(10L)
                .orderCode("ORD-001")
                .status(OrderStatus.OUT_FOR_DELIVERY)
                .tenantId(TENANT_ID)
                .build();
        InternalOrderStatusTransitionRequest.Context context =
                InternalOrderStatusTransitionRequest.Context.builder()
                        .tripCode("DLV-001")
                        .staffName("Courier A")
                        .build();

        when(transitionLogRepository.findByTenantIdAndIdempotencyKey(TENANT_ID, "last-mile:assigned"))
                .thenReturn(Optional.empty());
        when(orderRepository.findByIdAndTenantIdForUpdate(10L, TENANT_ID)).thenReturn(Optional.of(order));

        service.applyTransitions(InternalOrderStatusTransitionRequest.builder()
                .source("LAST_MILE_DELIVERY_DISPATCH")
                .idempotencyKey("last-mile:assigned")
                .items(List.of(InternalOrderStatusTransitionRequest.Item.builder()
                        .orderId(10L)
                        .expectedStatuses(List.of(OrderStatus.OUT_FOR_DELIVERY))
                        .targetStatus(OrderStatus.OUT_FOR_DELIVERY)
                        .description("Đơn hàng đã được điều phối cho bưu tá giao hàng.")
                        .recordTimelineWhenUnchanged(true)
                        .context(context)
                        .build()))
                .build(), TENANT_ID);

        assertEquals(OrderStatus.OUT_FOR_DELIVERY, order.getStatus());
        verify(orderTimelineService).recordStatusEvent(
                eq(order),
                eq(OrderStatus.OUT_FOR_DELIVERY),
                eq("Đơn hàng đã được điều phối cho bưu tá giao hàng."),
                eq(context)
        );
    }

    private InternalOrderStatusTransitionRequest transitionRequest(
            String idempotencyKey,
            OrderStatus expectedStatus,
            OrderStatus targetStatus
    ) {
        return transitionRequest(
                idempotencyKey,
                List.of(expectedStatus),
                targetStatus,
                11L,
                "HUB-11"
        );
    }

    private InternalOrderStatusTransitionRequest transitionRequest(
            String idempotencyKey,
            List<OrderStatus> expectedStatuses,
            OrderStatus targetStatus,
            Long hubId,
            String hubCode
    ) {
        return InternalOrderStatusTransitionRequest.builder()
                .source("SECOND_MILE")
                .idempotencyKey(idempotencyKey)
                .items(List.of(InternalOrderStatusTransitionRequest.Item.builder()
                        .orderId(10L)
                        .expectedStatuses(expectedStatuses)
                        .targetStatus(targetStatus)
                        .description("Bag distribution transition.")
                        .context(InternalOrderStatusTransitionRequest.Context.builder()
                                .hubId(hubId)
                                .hubCode(hubCode)
                                .build())
                        .build()))
                .build();
    }
}
