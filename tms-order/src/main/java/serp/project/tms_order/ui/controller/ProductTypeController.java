/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

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
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CreateProductTypeRequest;
import serp.project.tms_order.dto.request.UpdateProductTypeRequest;
import serp.project.tms_order.dto.response.ProductTypeResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.ProductTypeService;

@RestController
@RequestMapping("/api/v1/product-types")
@RequiredArgsConstructor
public class ProductTypeController {

    private final AuthUtils authUtils;
    private final MessageService messageService;
    private final ProductTypeService productTypeService;

    @GetMapping
    public ApiResponse<PageResponse<ProductTypeResponse>> getProductTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<PageResponse<ProductTypeResponse>>builder()
                .message(messageService.getMessage("success.product_types.list"))
                .result(productTypeService.getProductTypes(page, size, keyword, tenantId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductTypeResponse> getProductTypeById(@PathVariable Long id) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.detail"))
                .result(productTypeService.getProductTypeById(id, tenantId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<ProductTypeResponse> createProductType(@Valid @RequestBody CreateProductTypeRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.create"))
                .result(productTypeService.createProductType(request, tenantId))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<ProductTypeResponse> updateProductType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductTypeRequest request
    ) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<ProductTypeResponse>builder()
                .message(messageService.getMessage("success.product_types.update"))
                .result(productTypeService.updateProductType(id, request, tenantId))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<Void> deleteProductType(@PathVariable Long id) {
        Long tenantId = getCurrentTenantId();
        productTypeService.deleteProductType(id, tenantId);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.product_types.delete"))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
