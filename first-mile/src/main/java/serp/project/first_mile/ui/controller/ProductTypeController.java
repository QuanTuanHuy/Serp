/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreateProductTypeRequest;
import serp.project.first_mile.dto.request.UpdateProductTypeRequest;
import serp.project.first_mile.dto.response.ProductTypeResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.ProductTypeService;

@RestController
@RequestMapping("/api/v1/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final ProductTypeService productTypeService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<PageResponse<ProductTypeResponse>> getProductTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<PageResponse<ProductTypeResponse>>builder()
                .message(messageService.getMessage("success.product_types.list"))
                .result(productTypeService.getProductTypes(page, size, keyword))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductTypeResponse> getProductTypeById(@PathVariable Long id) {
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.detail"))
                .result(productTypeService.getProductTypeById(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<ProductTypeResponse> createProductType(@Valid @RequestBody CreateProductTypeRequest request) {
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.create"))
                .result(productTypeService.createProductType(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<ProductTypeResponse> updateProductType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductTypeRequest request
    ) {
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.update"))
                .result(productTypeService.updateProductType(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deleteProductType(@PathVariable Long id) {
        productTypeService.deleteProductType(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.product_types.delete"))
                .build();
    }
}