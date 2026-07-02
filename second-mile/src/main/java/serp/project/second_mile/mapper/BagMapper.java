/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderStatusTransitionRequest;
import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.BagFilterRequest;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.response.BagOrderResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.enums.BagStatus;
import serp.project.second_mile.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import static serp.project.second_mile.kernel.utils.CommonValueUtils.normalizeText;
import static serp.project.second_mile.kernel.utils.CommonValueUtils.safeDouble;

public final class BagMapper {
    private BagMapper() {
    }

    public static Bag toEntity(CreateBagRequest request) {
        Bag bag = new Bag();
        bag.setBagCode(request.getBagCode());
        bag.setOriginHubId(request.getOriginHubId());
        bag.setDestinationType(request.getDestinationType());
        bag.setDestinationHubId(request.getDestinationHubId());
        bag.setDestinationPostOfficeCode(request.getDestinationPostOfficeCode());
        bag.setMaxWeight(request.getMaxWeight());
        bag.setMaxVolume(request.getMaxVolume());
        bag.setMaxOrders(request.getMaxOrders());
        bag.setStatus(request.getStatus() == null ? BagStatus.CREATED : request.getStatus());
        bag.setNote(request.getNote());
        return bag;
    }

    public static void mapForUpdate(UpdateBagRequest request, Bag bag) {
        bag.setBagCode(request.getBagCode());
        bag.setOriginHubId(request.getOriginHubId());
        bag.setDestinationType(request.getDestinationType());
        bag.setDestinationHubId(request.getDestinationHubId());
        bag.setDestinationPostOfficeCode(request.getDestinationPostOfficeCode());
        bag.setMaxWeight(request.getMaxWeight());
        bag.setMaxVolume(request.getMaxVolume());
        bag.setMaxOrders(request.getMaxOrders());
        bag.setStatus(request.getStatus());
        bag.setNote(request.getNote());
    }

    public static BagResponse toResponse(Bag bag, List<BagOrder> bagOrders) {
        return new BagResponse(
                bag.getId(),
                bag.getBagCode(),
                bag.getOriginHubId(),
                bag.getDestinationType(),
                bag.getDestinationHubId(),
                bag.getDestinationPostOfficeCode(),
                bag.getVehicleId(),
                bag.getRouteId(),
                bag.getMaxWeight(),
                bag.getMaxVolume(),
                bag.getMaxOrders(),
                bag.getCurrentWeight(),
                bag.getCurrentVolume(),
                bag.getCurrentOrders(),
                bag.getStatus(),
                bag.getSealedAt(),
                bag.getNote(),
                bagOrders.stream()
                        .map(BagMapper::toBagOrderResponse)
                        .toList(),
                bag.getCreatedAt(),
                bag.getUpdatedAt(),
                bag.getCreatedBy(),
                bag.getUpdatedBy(),
                bag.getTenantId()
        );
    }

    public static BagFilterRequest normalizeFilterRequest(BagFilterRequest filterRequest) {
        if (filterRequest == null) {
            return BagFilterRequest.builder().build();
        }

        return BagFilterRequest.builder()
                .keyword(normalizeText(filterRequest.getKeyword()))
                .bagCode(normalizeText(filterRequest.getBagCode()))
                .originHubId(filterRequest.getOriginHubId())
                .destinationType(filterRequest.getDestinationType())
                .destinationHubId(filterRequest.getDestinationHubId())
                .destinationPostOfficeCode(normalizeText(filterRequest.getDestinationPostOfficeCode()))
                .vehicleId(filterRequest.getVehicleId())
                .status(filterRequest.getStatus())
                .build();
    }

    public static BagOrder toBagOrder(Bag bag, TmsOrderOperationView order, Long tenantId) {
        return BagOrder.builder()
                .bag(bag)
                .tmsOrderId(order.getId())
                .orderCode(normalizeText(order.getOrderCode()))
                .customerOrderCode(normalizeText(order.getCustomerOrderCode()))
                .lastKnownStatus(statusName(order.getStatus()))
                .originPostOfficeCode(normalizeText(order.getOriginPostOfficeCode()))
                .destinationPostOfficeCode(normalizeText(order.getDestinationPostOfficeCode()))
                .totalWeightSnapshot(safeDouble(order.getTotalWeight()))
                .totalVolumeSnapshot(safeDouble(order.getTotalVolume()))
                .tenantId(tenantId)
                .build();
    }

    public static TmsOrderStatusTransitionRequest.Item toTransitionItem(
            TmsOrderOperationView order,
            OrderStatus targetStatus,
            List<OrderStatus> expectedStatuses,
            String description,
            TmsOrderStatusTransitionRequest.Context context
    ) {
        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(context)
                .build();
    }

    public static TmsOrderStatusTransitionRequest.Item toTransitionItem(
            BagOrder bagOrder,
            OrderStatus targetStatus,
            List<OrderStatus> expectedStatuses,
            String description,
            TmsOrderStatusTransitionRequest.Context context
    ) {
        return TmsOrderStatusTransitionRequest.Item.builder()
                .orderId(bagOrder.getTmsOrderId())
                .orderCode(bagOrder.getOrderCode())
                .expectedStatuses(expectedStatuses)
                .targetStatus(targetStatus)
                .description(description)
                .context(context)
                .build();
    }

    public static TmsOrderStatusTransitionRequest.Context toBagContext(
            Bag bag,
            Hub hub,
            Vehicle vehicle,
            Route route
    ) {
        return TmsOrderStatusTransitionRequest.Context.builder()
                .eventTime(LocalDateTime.now())
                .hubId(hub == null ? bag.getOriginHubId() : hub.getId())
                .hubCode(hub == null ? null : hub.getCode())
                .hubName(hub == null ? null : hub.getName())
                .bagId(bag.getId())
                .bagCode(bag.getBagCode())
                .routeId(route == null ? bag.getRouteId() : route.getId())
                .routeCode(route == null ? null : route.getRouteCode())
                .driverId(vehicle == null ? null : vehicle.getAssignedStaffId())
                .vehicleId(vehicle == null ? bag.getVehicleId() : vehicle.getId())
                .vehicleLicensePlate(vehicle == null ? null : vehicle.getLicensePlate())
                .build();
    }

    private static BagOrderResponse toBagOrderResponse(BagOrder bagOrder) {
        return new BagOrderResponse(
                bagOrder.getId(),
                bagOrder.getTmsOrderId(),
                bagOrder.getOrderCode()
        );
    }

    private static String statusName(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
