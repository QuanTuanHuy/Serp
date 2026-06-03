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
                order.getPickupMethod(),
                order.getCreatedBy(),
                order.getCreatedAt(),
                order.getTenantId()
        );
    }

    private static Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private static Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }
}
