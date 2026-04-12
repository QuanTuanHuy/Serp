package serp.project.logistics2.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics2.dto.request.*;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.OutboundShipmentEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.OutboundShipmentService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequestMapping("/logistics2/api/v1/outbound-shipment")
@RequiredArgsConstructor
@Validated
@Slf4j
public class OutboundShipmentController {

    private final OutboundShipmentService shipmentService;
    private final AuthUtils authUtils;

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
