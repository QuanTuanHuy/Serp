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
import serp.project.tms_order.caller.FirstMilePostOfficeCaller;
import serp.project.tms_order.caller.FirstMilePostOfficeSuggestionCaller;
import serp.project.tms_order.caller.dto.firstmile.DestinationPostOfficeReservationResponse;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.kafka.OrderEventDispatcher;
import serp.project.tms_order.kafka.OrderNotificationEventPublisher;
import serp.project.tms_order.kafka.OrderSyncEventPublisher;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.OrderTimelineService;
import serp.project.tms_order.service.order.OrderAccessPolicy;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDropOffServiceImplTest {

    private static final Long TENANT_ID = 9L;
    private static final Long CUSTOMER_USER_ID = 42L;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private FirstMilePostOfficeCaller firstMilePostOfficeCaller;

    @Mock
    private FirstMilePostOfficeSuggestionCaller firstMilePostOfficeSuggestionCaller;

    @Mock
    private OrderTimelineService orderTimelineService;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private OrderSyncEventPublisher orderSyncEventPublisher;

    @Mock
    private OrderNotificationEventPublisher orderNotificationEventPublisher;

    private OrderDropOffServiceImpl orderDropOffService;

    @BeforeEach
    void setUp() {
        orderDropOffService = new OrderDropOffServiceImpl(
                orderRepository,
                firstMilePostOfficeCaller,
                firstMilePostOfficeSuggestionCaller,
                orderTimelineService,
                new OrderAccessPolicy(authUtils),
                new OrderEventDispatcher(orderSyncEventPublisher, orderNotificationEventPublisher)
        );
    }

    @Test
    void confirmDropOffOrderReservesPostOfficeAndPublishesEvents() {
        Order order = dropOffOrder();
        DestinationPostOfficeReservationResponse destinationPostOffice =
                DestinationPostOfficeReservationResponse.builder()
                        .id(6L)
                        .code("PO-HCM-01")
                        .name("Ho Chi Minh 01")
                        .currentDeliveryLoad(9)
                        .deliveryCapacity(200)
                        .build();
        OriginPostOfficeReservationResponse originPostOffice = OriginPostOfficeReservationResponse.builder()
                .id(5L)
                .code("PO-HN-01")
                .name("Ha Noi 01")
                .currentLoad(3)
                .dailyCapacity(100)
                .build();
        ConfirmDropOffOrderRequest request = new ConfirmDropOffOrderRequest();
        request.setPostOfficeId(5L);

        when(authUtils.hasAnyRole("TMS_POSTOFFICER_MANAGER")).thenReturn(true);
        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(firstMilePostOfficeCaller.reserveBestDestinationPostOffice(10.7769D, 106.7009D))
                .thenReturn(destinationPostOffice);
        when(firstMilePostOfficeCaller.reserveDropOffOriginPostOffice(5L, 21.0278D, 105.8342D))
                .thenReturn(originPostOffice);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderConfirmationResponse response = orderDropOffService.confirmDropOffOrderAtPostOffice(
                1L,
                TENANT_ID,
                request
        );

        assertEquals(OrderStatus.AT_ORIGIN_POST_OFFICE, response.status());
        assertEquals("PO-HN-01", response.originPostOffice().code());
        assertEquals("PO-HCM-01", response.destinationPostOffice().code());
        assertEquals(true, order.getIsConfirm());
        verify(orderSyncEventPublisher).publish(order);
        verify(orderNotificationEventPublisher).publishOrderConfirmed(order);
    }

    @Test
    void getDropOffPostOfficeSuggestionsUsesNormalizedLimit() {
        Order order = dropOffOrder();
        when(orderRepository.findByIdAndTenantId(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);

        orderDropOffService.getDropOffPostOfficeSuggestions(1L, 100, TENANT_ID);

        verify(firstMilePostOfficeSuggestionCaller).getDropOffSuggestions(21.0278D, 105.8342D, 20);
    }

    private Order dropOffOrder() {
        return Order.builder()
                .id(1L)
                .orderCode("ORD-001")
                .customerOrderCode("CUS-001")
                .status(OrderStatus.CREATED)
                .isConfirm(false)
                .pickupMethod(OrderPickupMethod.DROP_OFF_AT_POST_OFFICE)
                .feePayer(FeePayer.RECEIVER)
                .paymentStatus(PaymentStatus.UNPAID)
                .senderLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(105.8342D, 21.0278D)))
                .receiverLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(106.7009D, 10.7769D)))
                .tenantId(TENANT_ID)
                .createdBy(String.valueOf(CUSTOMER_USER_ID))
                .build();
    }
}
