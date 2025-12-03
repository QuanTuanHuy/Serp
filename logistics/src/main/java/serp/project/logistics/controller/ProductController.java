package serp.project.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics.dto.request.ProductCreationForm;
import serp.project.logistics.dto.request.ProductUpdateForm;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.ProductEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.ProductService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequestMapping("/logistics/api/v1/product")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

        private final ProductService productService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createProduct(@RequestBody ProductCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ProductController] Create product {} for tenantId {}", form.getName(), tenantId);
                productService.createProduct(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Product created successfully"));
        }

        @PatchMapping("/update/{productId}")
        public ResponseEntity<GeneralResponse<?>> updateProduct(
                        @PathVariable String productId,
                        @RequestBody ProductUpdateForm form) {
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
