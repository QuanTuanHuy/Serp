/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.OrderHistory;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.repository.OrderHistoryRepository;
import serp.project.first_mile.service.OrderTimelineService;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTimelineServiceImpl implements OrderTimelineService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordStatusEvent(
            Order order,
            OrderStatus orderStatus,
            String description,
            OrderTimelineContext context
    ) {
        if (order == null || order.getId() == null) {
            return;
        }

        OrderTimelineContext effectiveContext = context == null ? OrderTimelineContext.empty() : context;

        OrderHistory history = OrderHistory.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .customerOrderCode(order.getCustomerOrderCode())
                .orderStatus(orderStatus == null ? order.getStatus() : orderStatus)
                .description(description)
                .postOfficeId(effectiveContext.postOfficeId())
                .postOfficeCode(effectiveContext.postOfficeCode())
                .postOfficeName(effectiveContext.postOfficeName())
                .staffId(effectiveContext.courierStaffId())
                .staffCode(effectiveContext.courierCode())
                .staffName(effectiveContext.courierName())
                .tripId(effectiveContext.tripId())
                .tripCode(effectiveContext.tripCode())
                .vehicleId(effectiveContext.vehicleId())
                .vehicleLicensePlate(effectiveContext.vehicleLicensePlate())
                .latitude(effectiveContext.latitude())
                .longitude(effectiveContext.longitude())
                .locationLabel(effectiveContext.locationLabel())
                .eventTime(
                        effectiveContext.eventTime() == null
                                ? LocalDateTime.now()
                                : effectiveContext.eventTime()
                )
                .build();
        history.setTenantId(order.getTenantId());

        orderHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderTimelineResponse> getTimeline(Long orderId, Long tenantId) {
        return orderHistoryRepository.findByOrderIdAndTenantIdOrderByEventTimeDescIdDesc(orderId, tenantId)
                .stream()
                .map(history -> new OrderTimelineResponse(
                        history.getId(),
                        history.getOrderId(),
                        history.getOrderCode(),
                        history.getCustomerOrderCode(),
                        history.getOrderStatus(),
                        history.getDescription(),
                        history.getEventTime(),
                        history.getCreatedBy(),
                        history.getTripId(),
                        history.getTripCode(),
                        history.getPostOfficeId(),
                        history.getPostOfficeCode(),
                        history.getPostOfficeName(),
                        history.getStaffId(),
                        history.getStaffCode(),
                        history.getStaffName(),
                        history.getVehicleId(),
                        history.getVehicleLicensePlate(),
                        history.getLatitude(),
                        history.getLongitude(),
                        history.getLocationLabel()
                ))
                .toList();
    }
}
