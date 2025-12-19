package serp.project.purchase_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.purchase_service.dto.request.CategoryForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.PageResponse;
import serp.project.purchase_service.entity.CategoryEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.CategoryService;
import serp.project.purchase_service.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-service/api/v1/category")
@Validated
@Slf4j
public class CategoryController {

        private final CategoryService categoryService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createCategory(@Valid @RequestBody CategoryForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CategoryController] Creating category {} for tenantId: {}", form.getName(), tenantId);
                categoryService.createCategory(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Category created successfully"));
        }

        @PatchMapping("/update/{categoryId}")
        public ResponseEntity<GeneralResponse<?>> updateCategory(@Valid @RequestBody CategoryForm form,
                        @PathVariable("categoryId") String categoryId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CategoryController] Updating category {} with ID {} for tenantId: {}", form.getName(),
                                categoryId,
                                tenantId);
                categoryService.updateCategory(categoryId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Category updated successfully"));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<CategoryEntity>>> getCategories(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String statusId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CategoryController] Retrieving categories of page {}/{} for tenantId: {}", page, size,
                                tenantId);
                Page<CategoryEntity> categories = categoryService.getCategories(
                                query,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                return ResponseEntity.ok(
                                GeneralResponse.success("Successfully get list of category page " + page,
                                                PageResponse.of(categories)));
        }

        @DeleteMapping("/delete/{categoryId}")
        public ResponseEntity<GeneralResponse<?>> deleteCategory(@PathVariable("categoryId") String categoryId) {
                log.error("Delete category is not implemented yet");
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
        }

        @GetMapping("/search/{categoryId}")
        public ResponseEntity<GeneralResponse<CategoryEntity>> getCategory(
                        @PathVariable("categoryId") String categoryId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[CategoryController] Retrieving category with ID {} for tenantId: {}", categoryId, tenantId);
                CategoryEntity category = categoryService.getCategory(categoryId, tenantId);
                if (category == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                return ResponseEntity.ok(GeneralResponse.success("Successfully get facility detail", category));
        }

}
