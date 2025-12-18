package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
