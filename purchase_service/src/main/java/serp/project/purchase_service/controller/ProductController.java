package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import serp.project.purchase_service.dto.request.ProductCreationForm;
import serp.project.purchase_service.dto.request.ProductUpdateForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.PageResponse;
import serp.project.purchase_service.entity.ProductEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.ProductService;
import serp.project.purchase_service.util.AuthUtils;

@RestController
@RequestMapping("/purchase-service/api/v1/product")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

        private final ProductService productService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createProduct(@Valid @RequestBody ProductCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ProductController] Create product {} for tenantId {}", form.getName(), tenantId);
                productService.createProduct(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product created successfully"));
        }

        @PatchMapping("/update/{productId}")
        public ResponseEntity<GeneralResponse<?>> updateProduct(
                        @PathVariable String productId,
                        @Valid @RequestBody ProductUpdateForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ProductController] Update product {} for tenantId {}", productId, tenantId);
                productService.updateProduct(productId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product updated successfully"));
        }

        @DeleteMapping("/delete/{productId}")
        public ResponseEntity<GeneralResponse<?>> deleteProduct(@PathVariable String productId) {
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
        }

        @GetMapping("/search/{productId}")
        public ResponseEntity<GeneralResponse<ProductEntity>> getProduct(@PathVariable String productId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ProductController] Get product {} for tenantId {}", productId, tenantId);
                ProductEntity entity = productService.getProduct(productId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Successfully return product", entity));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<ProductEntity>>> getProducts(
                        @RequestParam(required = false, defaultValue = "1") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String categoryId,
                        @RequestParam(required = false) String statusId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ProductController] Search products of page {}/{} for tenantId {}", page, size, tenantId);
                Page<ProductEntity> products = productService.findProducts(
                                query,
                                categoryId,
                                statusId,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                return ResponseEntity.ok(GeneralResponse.success("Successfully return products at page " + page,
                                PageResponse.of(products)));
        }
}
