/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import serp.project.first_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.first_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.service.dto.OrderTimelineContext;

import java.util.List;
import java.util.UUID;

final class PickupOrderTransitionFactory {

    private static final String TRANSITION_SOURCE = "FIRST_MILE_PICKUP_TRACKING";

    private PickupOrderTransitionFactory() {
    }

    static TmsOrderStatusTransitionRequest.Item item(
            TmsOrderOperationView order,
            List<OrderStatus> expectedStatuses,
            OrderStatus targetStatus,
            String description,
            OrderTimelineContext context
    ) {
        TmsOrderStatusTransitionRequest.Context transitionContext = context == null
                ? null
                : TmsOrderStatusTransitionRequest.Context.builder()
                .eventTime(context.eventTime())
                .tripId(context.tripId())
                .tripCode(context.tripCode())
                .postOfficeId(context.postOfficeId())
                .postOfficeCode(context.postOfficeCode())
                .postOfficeName(context.postOfficeName())
                .staffId(context.courierStaffId())
                .staffCode(context.courierCode())
                .staffName(context.courierName())
                .vehicleId(context.vehicleId())
                .vehicleLicensePlate(context.vehicleLicensePlate())
                .latitude(context.latitude())
                .longitude(context.longitude())
                .locationLabel(context.locationLabel())
                .build();

        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(transitionContext)
                .build();
    }

    static TmsOrderStatusTransitionRequest request(List<TmsOrderStatusTransitionRequest.Item> items) {
        return TmsOrderStatusTransitionRequest.builder()
                .source(TRANSITION_SOURCE)
                .idempotencyKey(TRANSITION_SOURCE + "-" + UUID.randomUUID())
                .items(items)
                .build();
    }
}
