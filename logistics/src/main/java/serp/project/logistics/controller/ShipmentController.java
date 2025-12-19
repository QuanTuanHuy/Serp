package serp.project.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import serp.project.logistics.dto.request.*;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.InventoryItemDetailEntity;
import serp.project.logistics.entity.ShipmentEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.ShipmentService;
import serp.project.logistics.util.AuthUtils;

import java.util.List;

@RestController
@RequestMapping("/logistics/api/v1/shipment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ShipmentController {

        private final ShipmentService shipmentService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createShipment(
                        @Valid @RequestBody ShipmentCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new RuntimeException("Unauthorized"));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new RuntimeException("Unauthorized"));
                log.info("[ShipmentController] Create shipment {} for userId {} and tenantId {}",
                                form.getShipmentName(), userId, tenantId);
                shipmentService.createShipment(form, userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Shipment created successfully"));
        }

        @PatchMapping("/update/{shipmentId}")
        public ResponseEntity<GeneralResponse<?>> updateShipment(
                        @Valid @RequestBody ShipmentUpdateForm form,
                        @PathVariable String shipmentId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new RuntimeException("Unauthorized"));
                log.info("[ShipmentController] Update shipment {} for tenantId {}", shipmentId, tenantId);
                shipmentService.updateShipment(shipmentId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Shipment updated successfully"));
        }

        @PatchMapping("/manage/{shipmentId}/import")
        public ResponseEntity<GeneralResponse<?>> importShipment(
                        @PathVariable String shipmentId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Long userId = authUtils.getCurrentUserId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Import shipment {} for userId {} and tenantId {}", shipmentId, userId,
                                tenantId);
                shipmentService.importShipment(shipmentId, userId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Shipment imported successfully"));
        }

        @DeleteMapping("/delete/{shipmentId}")
        public ResponseEntity<GeneralResponse<?>> deleteShipment(
                        @PathVariable String shipmentId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Delete shipment {} for tenantId {}", shipmentId, tenantId);
                shipmentService.deleteShipment(shipmentId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Shipment deleted successfully"));
        }

        @PostMapping("/create/{shipmentId}/add")
        public ResponseEntity<GeneralResponse<?>> addItemToShipment(
                        @PathVariable String shipmentId,
                        @Valid @RequestBody ShipmentCreationForm.InventoryItemDetail form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Add item to shipment {} for tenantId {}", shipmentId, tenantId);
                shipmentService.createInventoryItemDetails(form, shipmentId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Item added to shipment successfully"));
        }

        @PatchMapping("/update/{shipmentId}/update/{itemId}")
        public ResponseEntity<GeneralResponse<?>> updateItemInShipment(
                        @PathVariable String shipmentId,
                        @PathVariable String itemId,
                        @Valid @RequestBody InventoryItemDetailUpdateForm itemForm) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Update item {} in shipment {} for tenantId {}", itemId, shipmentId,
                                tenantId);
                shipmentService.updateInventoryItemDetail(itemId, itemForm, shipmentId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Item updated in shipment successfully"));
        }

        @PatchMapping("/update/{shipmentId}/delete/{itemId}")
        public ResponseEntity<GeneralResponse<?>> deleteItemFromShipment(
                        @PathVariable String shipmentId,
                        @PathVariable String itemId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Delete item {} from shipment {} for tenantId {}", itemId, shipmentId,
                                tenantId);
                shipmentService.deleteItem(itemId, shipmentId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Item removed from shipment successfully"));
        }

        @GetMapping("/search/{shipmentId}")
        public ResponseEntity<GeneralResponse<ShipmentEntity>> getShipmentDetail(
                        @PathVariable String shipmentId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Get shipment detail {} for tenantId {}", shipmentId, tenantId);
                ShipmentEntity shipment = shipmentService.getShipment(shipmentId, tenantId);
                if (shipment == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                List<InventoryItemDetailEntity> items = shipmentService.getItemsByShipmentId(shipmentId,
                                tenantId);
                shipment.setItems(items);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get shipment detail", shipment));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<ShipmentEntity>>> getShipments(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String statusId,
                        @RequestParam(required = false) String shipmentTypeId,
                        @RequestParam(required = false) String toCustomerId,
                        @RequestParam(required = false) String fromSupplierId,
                        @RequestParam(required = false) String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[OrderController] Search orders of page {}/{} for tenantId {}", page, size, tenantId);
                var orders = shipmentService.findShipments(
                                query,
                                shipmentTypeId,
                                fromSupplierId,
                                toCustomerId,
                                orderId,
                                statusId,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get orders", PageResponse.of(orders)));
        }

}
