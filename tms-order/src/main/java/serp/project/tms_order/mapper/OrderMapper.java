/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.mapper;

import org.locationtech.jts.geom.Point;
import serp.project.tms_order.domain.Dimension;
import serp.project.tms_order.domain.Order;
import serp.project.tms_order.domain.Product;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.enums.OrderPickupMethod;

import java.util.List;

public final class OrderMapper {
    private OrderMapper() {
    }

    public static OrderDetailResponse toOrderDetailResponse(Order order) {
        List<OrderDetailResponse.ProductItem> products = order.getProducts() == null
                ? List.of()
                : order.getProducts().stream()
                .map(OrderMapper::toOrderDetailProductItem)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                order.getIsConfirm(),
                order.getSenderName(),
                order.getSenderPhone(),
                order.getSenderProvinceCode(),
                order.getSenderWardCode(),
                order.getSenderAddressDetail(),
                toLatitude(order.getSenderLocation()),
                toLongitude(order.getSenderLocation()),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverProvinceCode(),
                order.getReceiverWardCode(),
                order.getReceiverAddressDetail(),
                toLatitude(order.getReceiverLocation()),
                toLongitude(order.getReceiverLocation()),
                order.getPickupTimeStart(),
                order.getPickupTimeEnd(),
                order.getDeliveryRequestTime(),
                order.getPickupMethod() == null ? OrderPickupMethod.COURIER_PICKUP : order.getPickupMethod(),
                order.getOrderType(),
                order.getOrderProductCategory(),
                order.getFeePayer(),
                order.getPaymentStatus(),
                order.getCodAmount(),
                order.getTotalWeight(),
                order.getTotalValue(),
                order.getTotalVolume(),
                getDimensionLength(order.getDimensions()),
                getDimensionWidth(order.getDimensions()),
                getDimensionHeight(order.getDimensions()),
                order.getBaseShippingFee(),
                order.getCodFee(),
                order.getExtraFee(),
                order.getTotalShippingFee(),
                order.getOriginPostOfficeCode(),
                order.getDestinationPostOfficeCode(),
                order.getPlannedRoute(),
                order.getCurrentHubId(),
                order.getCurrentHubCode(),
                order.getNote(),
                products,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCreatedBy(),
                order.getUpdatedBy(),
                order.getTenantId()
        );
    }

    private static OrderDetailResponse.ProductItem toOrderDetailProductItem(Product product) {
        ProductType productType = product.getProductType();
        return new OrderDetailResponse.ProductItem(
                product.getId(),
                product.getName(),
                product.getValue(),
                product.getQuantity(),
                product.getWeight(),
                productType == null ? null : productType.getId(),
                productType == null ? null : productType.getCode(),
                productType == null ? null : productType.getName()
        );
    }

    private static Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private static Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }

    private static Double getDimensionLength(Dimension dimension) {
        return dimension == null ? null : dimension.getLength();
    }

    private static Double getDimensionWidth(Dimension dimension) {
        return dimension == null ? null : dimension.getWidth();
    }

    private static Double getDimensionHeight(Dimension dimension) {
        return dimension == null ? null : dimension.getHeight();
    }
}
