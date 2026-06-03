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
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.enums.DeliveryRequestTime;
import serp.project.tms_order.enums.FeePayer;
import serp.project.tms_order.enums.OrderPickupMethod;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.OrderType;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.ProductTypeRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final Long TENANT_ID = 9L;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private AuthUtils authUtils;

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

    private String orderCodePrefix() {
        return "ORD" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
