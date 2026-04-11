/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

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
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.OrderExcelService;
import serp.project.first_mile.service.OrderImportExcelService;

import java.time.LocalDate;
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

    @InjectMocks
    private OrderServiceImpl orderService;

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

    private Point point(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
