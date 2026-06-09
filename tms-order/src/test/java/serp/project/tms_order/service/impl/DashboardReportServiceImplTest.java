/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_order.dto.request.DashboardFilterRequest;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.enums.PaymentStatus;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.repository.projection.OrderDashboardSliceProjection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardReportServiceImplTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthUtils authUtils;

    @Test
    void getOverviewCalculatesOrderVolumeDeliveryAndFinanceMetrics() {
        DashboardReportServiceImpl service = new DashboardReportServiceImpl(orderRepository, authUtils);
        DashboardFilterRequest filter = DashboardFilterRequest.builder()
                .fromDate(LocalDate.of(2026, 6, 1))
                .toDate(LocalDate.of(2026, 6, 8))
                .timezone("Asia/Saigon")
                .granularity("DAY")
                .build();

        when(authUtils.getAllRoles()).thenReturn(List.of("TMS_ADMIN"));
        when(orderRepository.findDashboardSlices(
                1L,
                LocalDateTime.of(2026, 6, 1, 0, 0),
                LocalDateTime.of(2026, 6, 9, 0, 0),
                List.of("__NO_POST_OFFICE__"),
                true,
                null
        )).thenReturn(List.of(
                order(1L, "ORD-1", OrderStatus.CREATED, 50_000L, 100_000L, PaymentStatus.UNPAID),
                order(2L, "ORD-2", OrderStatus.DELIVERED, 70_000L, 200_000L, PaymentStatus.PAID),
                order(3L, "ORD-3", OrderStatus.DELIVERY_FAILED, 60_000L, 150_000L, PaymentStatus.UNPAID),
                order(4L, "ORD-4", OrderStatus.RETURNED_TO_SENDER, 40_000L, 120_000L, PaymentStatus.UNPAID)
        ));
        when(orderRepository.countDashboardOrders(
                1L,
                LocalDateTime.of(2026, 5, 24, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                List.of("__NO_POST_OFFICE__"),
                true,
                null
        )).thenReturn(2L);

        var overview = service.getOverview(filter, 1L);

        assertEquals("ADMIN", overview.scope().accessLevel());
        assertEquals(4, overview.orderVolume().totalOrders());
        assertEquals(1, overview.orderVolume().newOrders());
        assertEquals(1, overview.orderVolume().completedOrders());
        assertEquals(1, overview.orderVolume().returnedOrders());
        assertEquals(100.0, overview.orderVolume().growthRatePercent());
        assertEquals(1, overview.deliverySuccess().deliveredOrders());
        assertEquals(1, overview.deliverySuccess().failedDeliveryOrders());
        assertEquals(1, overview.deliverySuccess().returnedOrders());
        assertEquals(33.33, overview.deliverySuccess().successRatePercent());
        assertEquals(570_000L, overview.finance().codAmount());
        assertEquals(200_000L, overview.finance().codCollected());
        assertEquals(370_000L, overview.finance().codPending());
    }

    @Test
    void getAlertsReturnsPickupSlaBreach() {
        DashboardReportServiceImpl service = new DashboardReportServiceImpl(orderRepository, authUtils);
        DashboardFilterRequest filter = DashboardFilterRequest.builder()
                .fromDate(LocalDate.now().minusDays(1))
                .toDate(LocalDate.now())
                .timezone("Asia/Saigon")
                .granularity("DAY")
                .build();
        OrderDashboardSliceProjection stalePickup = new TestOrderDashboardSlice(
                10L,
                "ORD-10",
                OrderStatus.ASSIGNED_TO_PICKUP,
                "PO-1",
                "PO-2",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(6),
                LocalDateTime.now().minusHours(2),
                30_000L,
                0L,
                PaymentStatus.UNPAID
        );

        when(authUtils.getAllRoles()).thenReturn(List.of("TMS_POSTOFFICER_MANAGER"));
        when(orderRepository.findDashboardSlices(
                1L,
                filter.getFromDate().atStartOfDay(),
                filter.getToDate().plusDays(1).atStartOfDay(),
                List.of("__NO_POST_OFFICE__"),
                true,
                null
        )).thenReturn(List.of(stalePickup));

        var alerts = service.getAlerts(filter, 1L, 10);

        assertEquals("POST_OFFICE_MANAGER", alerts.scope().accessLevel());
        assertEquals(1, alerts.items().size());
        assertEquals("PICKUP_SLA_BREACH", alerts.items().getFirst().type());
        assertTrue(alerts.items().getFirst().actionHref().contains("ORD-10"));
    }

    private static OrderDashboardSliceProjection order(
            Long id,
            String code,
            OrderStatus status,
            Long shippingFee,
            Long codAmount,
            PaymentStatus paymentStatus
    ) {
        return new TestOrderDashboardSlice(
                id,
                code,
                status,
                "PO-1",
                "PO-2",
                LocalDateTime.of(2026, 6, 2, 8, 0),
                LocalDateTime.of(2026, 6, 2, 9, 0),
                LocalDateTime.of(2026, 6, 2, 10, 0),
                shippingFee,
                codAmount,
                paymentStatus
        );
    }

    private record TestOrderDashboardSlice(
            Long id,
            String orderCode,
            OrderStatus status,
            String originPostOfficeCode,
            String destinationPostOfficeCode,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime pickupTimeEnd,
            Long totalShippingFee,
            Long codAmount,
            PaymentStatus paymentStatus
    ) implements OrderDashboardSliceProjection {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getOrderCode() {
            return orderCode;
        }

        @Override
        public String getCustomerOrderCode() {
            return null;
        }

        @Override
        public OrderStatus getStatus() {
            return status;
        }

        @Override
        public String getOriginPostOfficeCode() {
            return originPostOfficeCode;
        }

        @Override
        public String getDestinationPostOfficeCode() {
            return destinationPostOfficeCode;
        }

        @Override
        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Override
        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        @Override
        public LocalDateTime getPickupTimeEnd() {
            return pickupTimeEnd;
        }

        @Override
        public Long getTotalShippingFee() {
            return totalShippingFee;
        }

        @Override
        public Long getBaseShippingFee() {
            return null;
        }

        @Override
        public Long getCodFee() {
            return null;
        }

        @Override
        public Long getExtraFee() {
            return null;
        }

        @Override
        public Long getCodAmount() {
            return codAmount;
        }

        @Override
        public PaymentStatus getPaymentStatus() {
            return paymentStatus;
        }
    }
}
