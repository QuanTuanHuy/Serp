package serp.project.logistics2.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.InventoryItemEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.InventoryItemService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequestMapping("logistics2/api/v1/inventory-item")
@RequiredArgsConstructor
@Validated
@Slf4j
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;
    private final AuthUtils authUtils;

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
