/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.request.CancelOrderRequest;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.enums.DeliveryRequestTime;
import serp.project.first_mile.enums.FeePayer;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.OrderType;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.ProductTypeRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private OrderExcelService orderExcelService;

    @Mock
    private OrderImportExcelService orderImportExcelService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PostOfficeRepository postOfficeRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private AuthUtils authUtils;

    @Mock
    private PostOfficeStaffRepository postOfficeStaffRepository;

    @Mock
    private PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;

    @Mock
    private TripOrderRepository tripOrderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(true);
        when(authUtils.hasAnyRole("TMS_CUSTOMER")).thenReturn(false);
        when(authUtils.hasAnyRole("TMS_POSTOFFICER_MANAGER")).thenReturn(false);
        when(authUtils.hasAnyRole("TMS_POSTOFFICER")).thenReturn(false);
    }

    @Test
    void confirmOrderShouldAssignBestPostOfficeAndIncreaseLoad() {
        Long tenantId = 1L;
        Long orderId = 99L;

        Order order = new Order();
        order.setId(orderId);
        order.setOrderCode("FM000099");
        order.setCustomerOrderCode("CUS000099");
        order.setStatus(OrderStatus.CREATED);
        order.setSenderLocation(point(10.77371, 106.70098));

        PostOffice postOffice = new PostOffice();
        postOffice.setId(10L);
        postOffice.setCode("PO-HCM-01");
        postOffice.setName("Post Office 01");
        postOffice.setStatus(PostOfficeStatus.ACTIVE);
        postOffice.setCurrentLoad(4);
        postOffice.setDailyCapacity(10);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));
        when(postOfficeRepository.findBestAssignablePostOfficeForSenderForUpdate(
                eq(tenantId),
                eq(order.getSenderLocation()),
                any(LocalDate.class)
        )).thenReturn(Optional.of(postOffice));

        OrderConfirmationResponse response = orderService.confirmOrder(orderId, tenantId);

        assertEquals("PO-HCM-01", order.getOriginPostOfficeCode());
        assertTrue(Boolean.TRUE.equals(order.getIsConfirm()));
        assertEquals(5, postOffice.getCurrentLoad());
        assertFalse(response.alreadyConfirmed());
        assertEquals("PO-HCM-01", response.originPostOffice().code());
        assertEquals(5, response.originPostOffice().currentLoad());

        verify(postOfficeRepository).save(postOffice);
        verify(orderRepository).save(order);
    }

    @Test
    void confirmOrderShouldThrowWhenNoSuitablePostOfficeExists() {
        Long tenantId = 1L;
        Long orderId = 100L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);
        order.setSenderLocation(point(10.77371, 106.70098));

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));
        when(postOfficeRepository.findBestAssignablePostOfficeForSenderForUpdate(
                eq(tenantId),
                eq(order.getSenderLocation()),
                any(LocalDate.class)
        )).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> orderService.confirmOrder(orderId, tenantId));

        assertEquals(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE, exception.getErrorCode());
        verify(postOfficeRepository, never()).save(any(PostOffice.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void confirmOrderShouldBeIdempotentWhenOriginPostOfficeAlreadyAssigned() {
        Long tenantId = 1L;
        Long orderId = 101L;

        Order order = new Order();
        order.setId(orderId);
        order.setOrderCode("FM000101");
        order.setCustomerOrderCode("CUS000101");
        order.setStatus(OrderStatus.ASSIGNED_TO_PICKUP);
        order.setIsConfirm(true);
        order.setOriginPostOfficeCode("PO-HCM-02");

        PostOffice postOffice = new PostOffice();
        postOffice.setId(11L);
        postOffice.setCode("PO-HCM-02");
        postOffice.setName("Post Office 02");
        postOffice.setCurrentLoad(8);
        postOffice.setDailyCapacity(20);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));
        when(postOfficeRepository.findByCodeIgnoreCaseAndTenantId("PO-HCM-02", tenantId))
                .thenReturn(Optional.of(postOffice));

        OrderConfirmationResponse response = orderService.confirmOrder(orderId, tenantId);

        assertTrue(response.alreadyConfirmed());
        assertEquals("PO-HCM-02", response.originPostOffice().code());
        assertEquals(8, response.originPostOffice().currentLoad());

        verify(postOfficeRepository, never()).findBestAssignablePostOfficeForSenderForUpdate(
                eq(tenantId),
                any(Point.class),
                any(LocalDate.class)
        );
        verify(postOfficeRepository, never()).save(any(PostOffice.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void confirmOrderShouldMarkConfirmedWhenOriginExistsButFlagIsFalse() {
        Long tenantId = 1L;
        Long orderId = 102L;

        Order order = new Order();
        order.setId(orderId);
        order.setOrderCode("FM000102");
        order.setCustomerOrderCode("CUS000102");
        order.setStatus(OrderStatus.CREATED);
        order.setIsConfirm(false);
        order.setOriginPostOfficeCode("PO-HCM-03");

        PostOffice postOffice = new PostOffice();
        postOffice.setId(12L);
        postOffice.setCode("PO-HCM-03");
        postOffice.setName("Post Office 03");
        postOffice.setCurrentLoad(6);
        postOffice.setDailyCapacity(20);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));
        when(postOfficeRepository.findByCodeIgnoreCaseAndTenantId("PO-HCM-03", tenantId))
                .thenReturn(Optional.of(postOffice));

        OrderConfirmationResponse response = orderService.confirmOrder(orderId, tenantId);

        assertTrue(Boolean.TRUE.equals(order.getIsConfirm()));
        assertTrue(response.alreadyConfirmed());
        assertEquals("PO-HCM-03", response.originPostOffice().code());

        verify(orderRepository).save(order);
        verify(postOfficeRepository, never()).findBestAssignablePostOfficeForSenderForUpdate(
                eq(tenantId),
                any(Point.class),
                any(LocalDate.class)
        );
        verify(postOfficeRepository, never()).save(any(PostOffice.class));
    }

    @Test
    void updateOrderShouldThrowWhenStatusIsNotCreated() {
        Long tenantId = 1L;
        Long orderId = 103L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.ASSIGNED_TO_PICKUP);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.updateOrder(orderId, buildValidUpdateRequest(), tenantId)
        );

        assertEquals(ErrorCode.ORDER_NOT_EDITABLE, exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderShouldThrowWhenStatusIsNotCreated() {
        Long tenantId = 1L;
        Long orderId = 104L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PICKUP_FAILED);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(
                AppException.class,
            () -> orderService.cancelOrder(orderId, tenantId, new CancelOrderRequest())
        );

        assertEquals(ErrorCode.ORDER_NOT_EDITABLE, exception.getErrorCode());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void cancelOrderShouldSetStatusCancelledWhenOrderIsCreated() {
        Long tenantId = 1L;
        Long orderId = 105L;

        Order order = new Order();
        order.setId(orderId);
        order.setOrderCode("FM000105");
        order.setCustomerOrderCode("CUS000105");
        order.setStatus(OrderStatus.CREATED);
        order.setIsConfirm(false);

        when(orderRepository.findByIdAndTenantIdForUpdate(orderId, tenantId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderDetailResponse response = orderService.cancelOrder(orderId, tenantId, new CancelOrderRequest("test"));

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(OrderStatus.CANCELLED, response.status());
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderByIdShouldThrowUnauthorizedWhenCustomerAccessesOtherOrder() {
        Long tenantId = 1L;
        Long orderId = 106L;

        Order order = new Order();
        order.setId(orderId);
        order.setCreatedBy("999");

        when(authUtils.hasAnyRole("TMS_ADMIN")).thenReturn(false);
        when(authUtils.hasAnyRole("TMS_CUSTOMER")).thenReturn(true);
        when(authUtils.getCurrentUserId()).thenReturn(Optional.of(1000L));
        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.getOrderById(orderId, tenantId)
        );

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    private UpdateOrderRequest buildValidUpdateRequest() {
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setCustomerOrderCode("CUS-UPD-001");
        request.setSenderName("Sender");
        request.setSenderPhone("0900000000");
        request.setSenderProvinceCode("79");
        request.setSenderWardCode("26734");
        request.setSenderAddressDetail("Sender address");
        request.setSenderLatitude(10.7769);
        request.setSenderLongitude(106.7009);
        request.setReceiverName("Receiver");
        request.setReceiverPhone("0911111111");
        request.setReceiverProvinceCode("79");
        request.setReceiverWardCode("26749");
        request.setReceiverAddressDetail("Receiver address");
        request.setReceiverLatitude(10.7821);
        request.setReceiverLongitude(106.6936);
        request.setDeliveryRequestTime(DeliveryRequestTime.FULL_DAY);
        request.setOrderType(OrderType.STANDARD_ORDER);
        request.setFeePayer(FeePayer.SENDER);
        request.setProducts(List.of());
        return request;
    }

    private Point point(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
