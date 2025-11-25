package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.purchase_service.dto.request.*;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.ShipmentDetailResponse;
import serp.project.purchase_service.entity.InventoryItemDetailEntity;
import serp.project.purchase_service.entity.ShipmentEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.InventoryItemDetailService;
import serp.project.purchase_service.service.ShipmentService;
import serp.project.purchase_service.util.AuthUtils;

import java.util.List;

@RestController
@RequestMapping("/purchase-service/api/v1/shipment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ShipmentController {

        private final ShipmentService shipmentService;
        private final InventoryItemDetailService inventoryItemDetailService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createShipment(
                        @RequestBody ShipmentCreationForm form) {
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
                        @RequestBody ShipmentUpdateForm form,
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
                        @RequestBody ShipmentItemAddForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Add item to shipment {} for tenantId {}", shipmentId, tenantId);
                ShipmentCreationForm.InventoryItemDetail itemForm = form.toInventoryItemDetailForm();
                inventoryItemDetailService.createInventoryItemDetails(shipmentId, itemForm, form.getFacilityId(),
                                tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Item added to shipment successfully"));
        }

        @PatchMapping("/update/{shipmentId}/update/{itemId}")
        public ResponseEntity<GeneralResponse<?>> updateItemInShipment(
                        @PathVariable String shipmentId,
                        @PathVariable String itemId,
                        @RequestBody InventoryItemDetailUpdateForm itemForm) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Update item {} in shipment {} for tenantId {}", itemId, shipmentId,
                                tenantId);
                inventoryItemDetailService.updateInventoryItemDetail(itemId, itemForm, shipmentId, tenantId);
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
                inventoryItemDetailService.deleteItem(itemId, shipmentId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Item removed from shipment successfully"));
        }

        @PatchMapping("/update/{shipmentId}/facility")
        public ResponseEntity<GeneralResponse<?>> updateShipmentFacility(
                        @PathVariable String shipmentId,
                        @RequestBody ShipmentFacilityUpdateForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Update facility of shipment {} to {} for tenantId {}", shipmentId,
                                form.getFacilityId(), tenantId);
                shipmentService.updateFacility(shipmentId, form.getFacilityId(), tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Shipment facility updated successfully"));
        }

        @GetMapping("/search/{shipmentId}")
        public ResponseEntity<GeneralResponse<ShipmentDetailResponse>> getShipmentDetail(
                        @PathVariable String shipmentId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Get shipment detail {} for tenantId {}", shipmentId, tenantId);
                ShipmentEntity shipment = shipmentService.getShipment(shipmentId, tenantId);
                List<InventoryItemDetailEntity> items = inventoryItemDetailService.getItemsByShipmentId(shipmentId,
                                tenantId);
                ShipmentDetailResponse response = ShipmentDetailResponse.fromEntity(shipment, items);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get shipment detail", response));
        }

        @GetMapping("/search/by-order/{orderId}")
        public ResponseEntity<GeneralResponse<List<ShipmentEntity>>> getShipmentsByOrderId(
                        @PathVariable String orderId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[ShipmentController] Get shipments by order ID {} for tenantId {}", orderId, tenantId);
                List<ShipmentEntity> shipments = shipmentService.findByOrderId(orderId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get shipments by order ID", shipments));
        }

}
