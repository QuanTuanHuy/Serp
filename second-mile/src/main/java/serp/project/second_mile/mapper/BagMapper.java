/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import serp.project.second_mile.domain.Bag;
import serp.project.second_mile.domain.BagOrder;
import serp.project.second_mile.dto.request.CreateBagRequest;
import serp.project.second_mile.dto.request.UpdateBagRequest;
import serp.project.second_mile.dto.response.BagOrderResponse;
import serp.project.second_mile.dto.response.BagResponse;
import serp.project.second_mile.enums.BagStatus;

import java.util.List;

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
        bag.setVehicleId(request.getVehicleId());
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
        bag.setVehicleId(request.getVehicleId());
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

    private static BagOrderResponse toBagOrderResponse(BagOrder bagOrder) {
        return new BagOrderResponse(
                bagOrder.getId(),
                bagOrder.getTmsOrderId(),
                bagOrder.getOrderCode()
        );
    }
}
