/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.mapper;

import serp.project.tms_order.domain.ProductType;
import serp.project.tms_order.dto.request.CreateProductTypeRequest;
import serp.project.tms_order.dto.request.UpdateProductTypeRequest;
import serp.project.tms_order.dto.response.ProductTypeResponse;

public final class ProductTypeMapper {

    private ProductTypeMapper() {
    }

    public static ProductType toEntity(CreateProductTypeRequest request) {
        return ProductType.builder()
                .code(request.getCode() == null ? null : request.getCode().trim())
                .name(request.getName() == null ? null : request.getName().trim())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();
    }

    public static void mapForUpdate(UpdateProductTypeRequest request, ProductType productType) {
        productType.setCode(request.getCode() == null ? null : request.getCode().trim());
        productType.setName(request.getName() == null ? null : request.getName().trim());
        productType.setIsActive(request.getIsActive() == null || request.getIsActive());
    }

    public static ProductTypeResponse toResponse(ProductType productType) {
        return new ProductTypeResponse(
                productType.getId(),
                productType.getCode(),
                productType.getName(),
                productType.getIsActive(),
                productType.getCreatedAt(),
                productType.getUpdatedAt(),
                productType.getCreatedBy(),
                productType.getUpdatedBy(),
                productType.getTenantId()
        );
    }
}
