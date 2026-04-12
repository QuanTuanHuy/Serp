package serp.project.logistics.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics.dto.request.*;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.OutboundShipmentEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.OutboundShipmentService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequestMapping("/logistics/api/v1/outbound-shipment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OutboundShipmentController {

    private final OutboundShipmentService shipmentService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createShipment(
            @Valid @RequestBody OutboundShipmentCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Create shipment {} by userId {} and tenantId {}",
                form.getName(), userId, tenantId);
        shipmentService.createShipment(form, userId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Shipment created successfully"));
    }

    @PatchMapping("/update/{shipmentId}")
    public ResponseEntity<GeneralResponse<?>> updateShipment(
            @Valid @RequestBody OutboundShipmentUpdateForm form,
            @PathVariable String shipmentId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Update shipment {} for tenantId {}", shipmentId, tenantId);
        shipmentService.updateShipment(shipmentId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Shipment updated successfully"));
    }

    @PatchMapping("/manage/{shipmentId}/ready")
    public ResponseEntity<GeneralResponse<?>> readyToExportShipment(
            @PathVariable String shipmentId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Import shipment {} for userId {} and tenantId {}", shipmentId, userId,
                tenantId);
        shipmentService.readyToExportShipment(shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Shipment ready to export"));
    }

    @DeleteMapping("/delete/{shipmentId}")
    public ResponseEntity<GeneralResponse<?>> deleteShipment(
            @PathVariable String shipmentId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Delete shipment {} for tenantId {}", shipmentId, tenantId);
        shipmentService.deleteShipment(shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Shipment deleted successfully"));
    }

    @PostMapping("/create/{shipmentId}/add")
    public ResponseEntity<GeneralResponse<?>> addItemToShipment(
            @PathVariable String shipmentId,
            @Valid @RequestBody OutboundShipmentCreationForm.ItemForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Add item to shipment {} for tenantId {}", shipmentId, tenantId);
        shipmentService.addShipmentItem(form, shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Item added to shipment successfully"));
    }

    @PatchMapping("/update/{shipmentId}/update/{itemId}")
    public ResponseEntity<GeneralResponse<?>> updateItemInShipment(
            @PathVariable String shipmentId,
            @PathVariable String itemId,
            @Valid @RequestBody OutboundShipmentItemUpdateForm itemForm) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Update item {} in shipment {} for tenantId {}", itemId, shipmentId,
                tenantId);
        shipmentService.updateShipmentItem(itemId, itemForm, shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Item updated in shipment successfully"));
    }

    @PatchMapping("/update/{shipmentId}/delete/{itemId}")
    public ResponseEntity<GeneralResponse<?>> deleteItemFromShipment(
            @PathVariable String shipmentId,
            @PathVariable String itemId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Delete item {} from shipment {} for tenantId {}", itemId, shipmentId,
                tenantId);
        shipmentService.deleteItem(itemId, shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Item removed from shipment successfully"));
    }

    @GetMapping("/search/{shipmentId}")
    public ResponseEntity<GeneralResponse<OutboundShipmentEntity>> getShipmentDetail(
            @PathVariable String shipmentId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OutboundShipmentController] Get shipment detail {} for tenantId {}", shipmentId, tenantId);
        OutboundShipmentEntity shipment = shipmentService.getShipment(shipmentId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get shipment detail", shipment));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<OutboundShipmentEntity>>> getShipments(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String facilityId,
            @RequestParam(required = false) String orderId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[OrderController] Search shipments of page {}/{} for tenantId {}", page, size, tenantId);
        var shipments = shipmentService.findShipments(
                status,
                orderId,
                facilityId,
                query,
                tenantId,
                page,
                size,
                sortBy,
                sortDirection);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get outbound shipments", PageResponse.of(shipments)));
    }

}
