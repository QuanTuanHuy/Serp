/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.mapper;

import serp.project.tms_order.domain.Product;
import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.service.order.OrderTextUtils;

public final class OrderProductMapper {

    private OrderProductMapper() {
    }

    public static Product toProduct(
            CreateOrderRequest.ProductItem item,
            ProductType productType,
            Long tenantId
    ) {
        return Product.builder()
                .name(OrderTextUtils.normalizeText(item.getName()))
                .value(item.getValue())
                .quantity(item.getQuantity())
                .weight(item.getWeightGram())
                .productType(productType)
                .tenantId(tenantId)
                .build();
    }
}
