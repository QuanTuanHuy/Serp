package serp.project.logistics.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics.dto.request.InventoryItemCreationForm;
import serp.project.logistics.dto.request.InventoryItemUpdateForm;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.InventoryItemEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.InventoryItemService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequestMapping("logistics/api/v1/inventory-item")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InventoryItemController {

        private final InventoryItemService inventoryItemService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createInventoryItem(
                        @Valid @RequestBody InventoryItemCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[InventoryItemController] Creating inventory item for product ID: {} for tenantId: {}",
                                form.getProductId(), tenantId);
                inventoryItemService.createInventoryItem(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Inventory item created successfully"));
        }

        @PatchMapping("/update/{inventoryItemId}")
        public ResponseEntity<GeneralResponse<?>> updateInventoryItem(
                        @Valid @RequestBody InventoryItemUpdateForm form,
                        @PathVariable("inventoryItemId") String inventoryItemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[InventoryItemController] Updating inventory item with ID {} for tenantId: {}",
                                inventoryItemId,
                                tenantId);
                inventoryItemService.updateInventoryItem(inventoryItemId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Inventory item updated successfully"));
        }

        @DeleteMapping("/delete/{inventoryItemId}")
        public ResponseEntity<GeneralResponse<?>> deleteInventoryItem(
                        @PathVariable("inventoryItemId") String inventoryItemId) {
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
        }

        @GetMapping("/search/{inventoryItemId}")
        public ResponseEntity<GeneralResponse<InventoryItemEntity>> searchInventoryItem(
                        @PathVariable("inventoryItemId") String inventoryItemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[InventoryItemController] Retrieving inventory item with ID {} for tenantId: {}",
                                inventoryItemId,
                                tenantId);
                return ResponseEntity.ok(GeneralResponse.success(
                                "Inventory item retrieved successfully",
                                inventoryItemService.getInventoryItem(inventoryItemId, tenantId)));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<InventoryItemEntity>>> searchInventoryItems(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String productId,
                        @RequestParam(required = false) String facilityId,
                        @RequestParam(required = false) LocalDate expirationDateFrom,
                        @RequestParam(required = false) LocalDate expirationDateTo,
                        @RequestParam(required = false) LocalDate manufacturingDateFrom,
                        @RequestParam(required = false) LocalDate manufacturingDateTo,
                        @RequestParam(required = false) String statusId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[InventoryItemController] Retrieving inventory items of page {}/{} for tenantId: {}", page,
                                size,
                                tenantId);
                Page<InventoryItemEntity> result = inventoryItemService.getInventoryItems(
                                query,
                                productId,
                                facilityId,
                                expirationDateFrom,
                                expirationDateTo,
                                manufacturingDateFrom,
                                manufacturingDateTo,
                                statusId,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                return ResponseEntity.ok(GeneralResponse.success(
                                "Inventory items retrieved successfully",
                                PageResponse.of(result)));
        }

}
