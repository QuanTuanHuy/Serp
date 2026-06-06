/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.tms_order.caller.FirstMilePostOfficeCaller;
import serp.project.tms_order.caller.PaymentServiceCaller;
import serp.project.tms_order.caller.dto.firstmile.DestinationPostOfficeReservationResponse;
import serp.project.tms_order.caller.dto.firstmile.OriginPostOfficeReservationResponse;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.PaymentOrderConfirmedWebhookRequest;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.PaymentWebhookProcessResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.enums.DeliveryRequestTime;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.OrderType;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kafka.OrderSyncEventPublisher;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.service.OrderTimelineService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long TENANT_ID = 9L;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private PaymentServiceCaller paymentServiceCaller;

    @Mock
    private FirstMilePostOfficeCaller firstMilePostOfficeCaller;

    @Mock
    private OrderSyncEventPublisher orderSyncEventPublisher;

    @Mock
    private OrderTimelineService orderTimelineService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderBuildsOrderAndAggregatesProducts() {
        ProductType productType = ProductType.builder()
                .id(7L)
                .code("BOX")
                .name("Box")
                .isActive(true)
                .build();

        when(orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantId("CUS-001", TENANT_ID))
                .thenReturn(false);
        when(orderRepository.findMaxOrderCodeByPrefix(orderCodePrefix()))
                .thenReturn(null);
        when(productTypeRepository.findByIdAndTenantId(7L, TENANT_ID))
                .thenReturn(Optional.of(productType));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderDetailResponse response = orderService.createOrder(createRequest(), TENANT_ID);

        assertTrue(response.orderCode().startsWith(orderCodePrefix()));
        assertTrue(response.orderCode().endsWith("0001"));
        assertEquals(OrderStatus.CREATED, response.status());
        assertEquals(false, response.isConfirm());
        assertEquals(OrderPickupMethod.COURIER_PICKUP, response.pickupMethod());
        assertEquals(PaymentStatus.UNPAID, response.paymentStatus());
        assertEquals(500.5D, response.totalWeight());
        assertEquals(20000D, response.totalValue());
        assertEquals(20000L, response.codAmount());
        assertEquals(0L, response.totalShippingFee());
        assertEquals(TENANT_ID, response.tenantId());
        assertNotNull(response.senderLatitude());
        assertNotNull(response.senderLongitude());
        assertEquals(1, response.products().size());
        assertEquals(7L, response.products().getFirst().productTypeId());
        assertEquals("BOX", response.products().getFirst().productTypeCode());
    }

    @Test
    void createOrderRejectsDuplicatedCustomerOrderCode() {
        when(orderRepository.existsByCustomerOrderCodeIgnoreCaseAndTenantId("CUS-001", TENANT_ID))
                .thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.createOrder(createRequest(), TENANT_ID)
        );

        assertEquals(ErrorCode.ORDER_CUSTOMER_CODE_EXISTED, exception.getErrorCode());
    }

    @Test
    void confirmOrderRequiresSenderPaymentBeforeConfirmation() {
        Order order = confirmableOrder();
        order.setFeePayer(FeePayer.SENDER);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.confirmOrder(1L, TENANT_ID)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(firstMilePostOfficeCaller, never()).reserveBestOriginPostOffice(any(), any());
    }

    @Test
    void confirmOrderReservesOriginPostOfficeAndPublishesSyncEvent() {
        Order order = confirmableOrder();
        OriginPostOfficeReservationResponse postOffice = OriginPostOfficeReservationResponse.builder()
                .id(5L)
                .code("PO-HN-01")
                .name("Ha Noi 01")
                .currentLoad(3)
                .dailyCapacity(100)
                .build();
        DestinationPostOfficeReservationResponse destinationPostOffice =
                DestinationPostOfficeReservationResponse.builder()
                        .id(6L)
                        .code("PO-HCM-01")
                        .name("Ho Chi Minh 01")
                        .currentDeliveryLoad(9)
                        .deliveryCapacity(200)
                        .build();

        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);
        when(firstMilePostOfficeCaller.reserveBestDestinationPostOffice(10.7769D, 106.7009D))
                .thenReturn(destinationPostOffice);
        when(firstMilePostOfficeCaller.reserveBestOriginPostOffice(21.0278D, 105.8342D))
                .thenReturn(postOffice);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderConfirmationResponse response = orderService.confirmOrder(1L, TENANT_ID);

        assertTrue(response.alreadyConfirmed() == false);
        assertEquals("PO-HN-01", response.originPostOffice().code());
        assertEquals("PO-HCM-01", response.destinationPostOffice().code());
        assertEquals(true, order.getIsConfirm());
        assertEquals("PO-HN-01", order.getOriginPostOfficeCode());
        assertEquals("PO-HCM-01", order.getDestinationPostOfficeCode());
        verify(orderSyncEventPublisher).publish(order);
    }

    @Test
    void confirmOrderPaymentReturnsSuccessWhenOrderAlreadyPaid() {
        Order order = confirmableOrder();
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdAndTenantIdForUpdate(1L, TENANT_ID))
                .thenReturn(Optional.of(order));
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);

        OrderPaymentConfirmResponse response = orderService.confirmOrderPayment(
                1L,
                TENANT_ID,
                new ConfirmOrderPaymentRequest("250101_abc")
        );

        assertEquals(PaymentStatus.PAID, response.paymentStatus());
        assertEquals("SUCCESS", response.gatewayStatus());
        verify(paymentServiceCaller, never()).queryOrderStatus(any());
        verify(orderSyncEventPublisher, never()).publish(any());
    }

    @Test
    void processPaymentWebhookMarksOrderPaidAndPublishesSyncEvent() {
        Order order = confirmableOrder();
        order.setPaymentStatus(PaymentStatus.UNPAID);

        when(orderRepository.findByOrderCodeAndTenantIdForUpdate("ORD-001", TENANT_ID))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentWebhookProcessResponse response = orderService.processPaymentOrderConfirmedWebhook(
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
    }

    private CreateOrderRequest createRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerOrderCode(" CUS-001 ");
        request.setSenderName("Sender");
        request.setSenderPhone("0900000000");
        request.setSenderProvinceCode("01");
        request.setSenderWardCode("00004");
        request.setSenderAddressDetail("Sender address");
        request.setSenderLatitude(21.0278D);
        request.setSenderLongitude(105.8342D);
        request.setReceiverName("Receiver");
        request.setReceiverPhone("0911111111");
        request.setReceiverProvinceCode("79");
        request.setReceiverWardCode("26734");
        request.setReceiverAddressDetail("Receiver address");
        request.setReceiverLatitude(10.7769D);
        request.setReceiverLongitude(106.7009D);
        request.setDeliveryRequestTime(DeliveryRequestTime.FULL_DAY);
        request.setOrderType(OrderType.STANDARD_ORDER);
        request.setFeePayer(FeePayer.SENDER);
        request.setIsCod(true);
        request.setDimensionLengthCm(10D);
        request.setDimensionWidthCm(20D);
        request.setDimensionHeightCm(30D);
        request.setTotalVolumeM3(0.006D);
        request.setProducts(List.of(new CreateOrderRequest.ProductItem(
                "Box",
                10000L,
                2,
                250.25D,
                7L
        )));
        return request;
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
                .build();
    }

    private String orderCodePrefix() {
        return "ORD" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
