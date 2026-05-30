package serp.project.first_mile.mapper;

import org.locationtech.jts.geom.Point;
import serp.project.first_mile.domain.Dimension;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Product;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.dto.request.CreateOrderRequest;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.enums.OrderPickupMethod;
import serp.project.first_mile.service.dto.ManualOrderPayload;
import serp.project.first_mile.service.dto.ManualOrderProductPayload;

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
                order.getOrderProductCategory(),
                order.getOrderType(),
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

    public static OrderConfirmationResponse toOrderConfirmationResponse(
            Order order,
            PostOffice postOffice,
            boolean alreadyConfirmed
    ) {
        String postOfficeCode = postOffice == null
                ? order.getOriginPostOfficeCode()
                : postOffice.getCode();

        OrderConfirmationResponse.OriginPostOfficeInfo originInfo =
                new OrderConfirmationResponse.OriginPostOfficeInfo(
                        postOffice == null ? null : postOffice.getId(),
                        postOfficeCode,
                        postOffice == null ? null : postOffice.getName(),
                        postOffice == null ? null : postOffice.getCurrentLoad(),
                        postOffice == null ? null : postOffice.getDailyCapacity()
                );

        return new OrderConfirmationResponse(
                order.getId(),
                order.getOrderCode(),
                order.getCustomerOrderCode(),
                order.getStatus(),
                alreadyConfirmed,
                originInfo
        );
    }

    public static ManualOrderPayload toManualOrderPayload(CreateOrderRequest request) {
        List<ManualOrderProductPayload> products = request.getProducts() == null
                ? List.of()
                : request.getProducts().stream()
                  .map(OrderMapper::toManualOrderProductPayload)
                  .toList();

        return new ManualOrderPayload(
                normalizeText(request.getCustomerOrderCode()),
                normalizeText(request.getSenderName()),
                normalizeText(request.getSenderPhone()),
                normalizeText(request.getSenderProvinceCode()),
                normalizeText(request.getSenderWardCode()),
                normalizeText(request.getSenderAddressDetail()),
                request.getSenderLatitude(),
                request.getSenderLongitude(),
                normalizeText(request.getReceiverName()),
                normalizeText(request.getReceiverPhone()),
                normalizeText(request.getReceiverProvinceCode()),
                normalizeText(request.getReceiverWardCode()),
                normalizeText(request.getReceiverAddressDetail()),
                request.getReceiverLatitude(),
                request.getReceiverLongitude(),
                request.getPickupTimeStart(),
                request.getPickupTimeEnd(),
                request.getDeliveryRequestTime(),
                request.getPickupMethod(),
                request.getOrderProductCategory(),
                request.getOrderType(),
                request.getFeePayer(),
                request.getIsCod(),
                request.getDimensionLengthCm(),
                request.getDimensionWidthCm(),
                request.getDimensionHeightCm(),
                request.getTotalVolumeM3(),
                normalizeText(request.getNote()),
                products
        );
    }

    public static ManualOrderPayload toManualOrderPayload(UpdateOrderRequest request) {
        List<ManualOrderProductPayload> products = request.getProducts() == null
                ? List.of()
                : request.getProducts().stream()
                  .map(OrderMapper::toManualOrderProductPayload)
                  .toList();

        return new ManualOrderPayload(
                normalizeText(request.getCustomerOrderCode()),
                normalizeText(request.getSenderName()),
                normalizeText(request.getSenderPhone()),
                normalizeText(request.getSenderProvinceCode()),
                normalizeText(request.getSenderWardCode()),
                normalizeText(request.getSenderAddressDetail()),
                request.getSenderLatitude(),
                request.getSenderLongitude(),
                normalizeText(request.getReceiverName()),
                normalizeText(request.getReceiverPhone()),
                normalizeText(request.getReceiverProvinceCode()),
                normalizeText(request.getReceiverWardCode()),
                normalizeText(request.getReceiverAddressDetail()),
                request.getReceiverLatitude(),
                request.getReceiverLongitude(),
                request.getPickupTimeStart(),
                request.getPickupTimeEnd(),
                request.getDeliveryRequestTime(),
                request.getPickupMethod(),
                request.getOrderProductCategory(),
                request.getOrderType(),
                request.getFeePayer(),
                request.getIsCod(),
                request.getDimensionLengthCm(),
                request.getDimensionWidthCm(),
                request.getDimensionHeightCm(),
                request.getTotalVolumeM3(),
                normalizeText(request.getNote()),
                products
        );
    }

    private static ManualOrderProductPayload toManualOrderProductPayload(CreateOrderRequest.ProductItem productItem) {
        return new ManualOrderProductPayload(
                normalizeText(productItem.getName()),
                productItem.getValue(),
                productItem.getQuantity(),
                productItem.getWeightGram(),
                productItem.getProductTypeId()
        );
    }

    private static ManualOrderProductPayload toManualOrderProductPayload(UpdateOrderRequest.ProductItem productItem) {
        return new ManualOrderProductPayload(
                normalizeText(productItem.getName()),
                productItem.getValue(),
                productItem.getQuantity(),
                productItem.getWeightGram(),
                productItem.getProductTypeId()
        );
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
