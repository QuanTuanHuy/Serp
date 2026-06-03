/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service;

import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CreateProductTypeRequest;
import serp.project.tms_order.dto.request.UpdateProductTypeRequest;
import serp.project.tms_order.dto.response.ProductTypeResponse;

public interface ProductTypeService {
    PageResponse<ProductTypeResponse> getProductTypes(int page, int size, String keyword, Long tenantId);

    ProductTypeResponse getProductTypeById(Long id, Long tenantId);

    ProductTypeResponse createProductType(CreateProductTypeRequest request, Long tenantId);

    ProductTypeResponse updateProductType(Long id, UpdateProductTypeRequest request, Long tenantId);

    void deleteProductType(Long id, Long tenantId);
}
