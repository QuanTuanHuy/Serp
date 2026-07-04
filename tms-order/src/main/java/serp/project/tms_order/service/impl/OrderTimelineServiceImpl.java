/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.OrderHistory;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.dto.response.OrderTimelineResponse;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.repository.OrderHistoryRepository;
import serp.project.tms_order.repository.OrderRepository;
import serp.project.tms_order.service.OrderTimelineService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderTimelineServiceImpl implements OrderTimelineService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordStatusEvent(
            Order order,
            OrderStatus orderStatus,
            String description,
            InternalOrderStatusTransitionRequest.Context context
    ) {
        if (order == null || order.getId() == null) {
            return;
        }

        InternalOrderStatusTransitionRequest.Context safeContext = context == null
                ? InternalOrderStatusTransitionRequest.Context.builder().build()
                : context;

        OrderHistory history = OrderHistory.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .customerOrderCode(order.getCustomerOrderCode())
                .orderStatus(orderStatus == null ? order.getStatus() : orderStatus)
                .description(normalizeText(description))
                .postOfficeId(safeContext.getPostOfficeId())
                .postOfficeCode(resolvePostOfficeCode(order, safeContext))
                .postOfficeName(safeContext.getPostOfficeName())
                .staffId(safeContext.getStaffId())
                .staffCode(safeContext.getStaffCode())
                .staffName(safeContext.getStaffName())
                .tripId(safeContext.getTripId())
                .tripCode(safeContext.getTripCode())
                .vehicleId(safeContext.getVehicleId())
                .vehicleLicensePlate(safeContext.getVehicleLicensePlate())
                .latitude(safeContext.getLatitude())
                .longitude(safeContext.getLongitude())
                .locationLabel(safeContext.getLocationLabel())
                .eventTime(safeContext.getEventTime() == null ? LocalDateTime.now() : safeContext.getEventTime())
                .tenantId(order.getTenantId())
                .build();

        orderHistoryRepository.save(history);
    }

    @Override
    public List<OrderTimelineResponse> getTimeline(Long orderId, Long tenantId) {
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderTimelineResponse> timeline = orderHistoryRepository
                .findByOrderIdAndTenantIdOrderByEventTimeDescIdDesc(orderId, tenantId)
                .stream()
                .map(this::toResponse)
                .toList();

        if (!timeline.isEmpty()) {
            return timeline;
        }

        List<OrderTimelineResponse> fallback = new ArrayList<>();
        fallback.add(new OrderTimelineResponse(
                null,
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus() == null ? OrderStatus.CREATED : order.getStatus(),
                "Order created.",
                order.getCreatedAt(),
                order.getCreatedBy(),
                null,
                null,
                null,
                order.getOriginPostOfficeCode(),
                null,
                null,
                null,
                null,
                null,
                null,
                toLatitude(order.getSenderLocation()),
                toLongitude(order.getSenderLocation()),
                buildSenderLocationLabel(order)
        ));
        return fallback;
    }

    private OrderTimelineResponse toResponse(OrderHistory history) {
        return new OrderTimelineResponse(
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
        );
    }

    private String resolvePostOfficeCode(Order order, InternalOrderStatusTransitionRequest.Context context) {
        if (hasText(context.getPostOfficeCode())) {
            return context.getPostOfficeCode().trim();
        }
        return order == null ? null : order.getOriginPostOfficeCode();
    }

    private String buildSenderLocationLabel(Order order) {
        if (order == null) {
            return null;
        }

        List<String> addressParts = new ArrayList<>();
        if (hasText(order.getSenderAddressDetail())) {
            addressParts.add(order.getSenderAddressDetail().trim());
        }
        if (hasText(order.getSenderWardCode())) {
            addressParts.add(order.getSenderWardCode().trim());
        }
        if (hasText(order.getSenderProvinceCode())) {
            addressParts.add(order.getSenderProvinceCode().trim());
        }
        return addressParts.isEmpty() ? null : String.join(", ", addressParts);
    }

    private Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
