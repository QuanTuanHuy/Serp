/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateProductTypeRequest;
import serp.project.first_mile.dto.request.UpdateProductTypeRequest;
import serp.project.first_mile.dto.response.ProductTypeResponse;

public interface ProductTypeService {
    PageResponse<ProductTypeResponse> getProductTypes(int page, int size, String keyword);

    ProductTypeResponse getProductTypeById(Long id);

    ProductTypeResponse createProductType(CreateProductTypeRequest request);

    ProductTypeResponse updateProductType(Long id, UpdateProductTypeRequest request);

    void deleteProductType(Long id);
}