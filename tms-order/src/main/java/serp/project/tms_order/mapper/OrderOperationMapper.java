/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.mapper;

import org.locationtech.jts.geom.Point;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.dto.response.OrderOperationView;

public final class OrderOperationMapper {
    private OrderOperationMapper() {
    }

    public static OrderOperationView toView(Order order) {
        return new OrderOperationView(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                order.getIsConfirm(),
                order.getOriginPostOfficeCode(),
                order.getDestinationPostOfficeCode(),
                order.getSenderName(),
                order.getSenderPhone(),
                order.getSenderProvinceCode(),
                order.getSenderWardCode(),
                order.getSenderAddressDetail(),
                toLatitude(order.getSenderLocation()),
                toLongitude(order.getSenderLocation()),
                order.getPickupTimeStart(),
                order.getPickupTimeEnd(),
                order.getTotalWeight(),
                order.getTotalVolume(),
                order.getDimensions(),
                order.getOrderProductCategory(),
                order.getOrderType(),
                order.getNote(),
                order.getPickupMethod(),
                order.getCreatedBy(),
                order.getCreatedAt(),
                order.getTenantId(),
                // Receiver info
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverWardCode(),
                order.getReceiverProvinceCode(),
                order.getReceiverAddressDetail(),
                toLatitude(order.getReceiverLocation()),
                toLongitude(order.getReceiverLocation()),
                // COD & fee
                order.getCodAmount(),
                order.getTotalShippingFee(),
                order.getFeePayer() != null ? order.getFeePayer().name() : null,
                order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null
        );
    }

    private static Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private static Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }
}
