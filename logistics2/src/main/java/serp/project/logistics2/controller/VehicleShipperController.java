package serp.project.logistics2.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.request.VehicleShipperAssignmentForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.VehicleShipperEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.orchestrator.RoutingOrchestrator;
import serp.project.logistics2.service.VehicleShipperService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/vehicle-shippers")
@Validated
@Slf4j
public class VehicleShipperController {

    private final VehicleShipperService vehicleShipperService;
    private final RoutingOrchestrator routingOrchestrator;
    private final AuthUtils authUtils;

    @PostMapping("/assign")
    public ResponseEntity<GeneralResponse<?>> assignVehicleToShipper(
            @Valid @RequestBody VehicleShipperAssignmentForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to assign vehicle {} to shipper {} for date {}", form.getVehicleId(), userId,
                form.getWorkingDate());
        vehicleShipperService.assignVehicleToShipper(form, userId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Gán phương tiện cho tài xế thành công"));
    }

    @PutMapping("/request-cancel/{vehicleShipperId}")
    public ResponseEntity<GeneralResponse<?>> requestCancelVehicleShipperRequest(
            @PathVariable String vehicleShipperId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to cancel vehicle shipper request for ID {}", vehicleShipperId);
        vehicleShipperService.requestCancelVehicleAssignment(vehicleShipperId, userId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Hủy yêu cầu gán phương tiện cho tài xế thành công"));
    }

    @PutMapping("/cancel/{vehicleShipperId}")
    public ResponseEntity<GeneralResponse<?>> cancelVehicleShipperRequest(
            @PathVariable String vehicleShipperId) {
        log.info("Received request to cancel vehicle shipper assignment for ID {}", vehicleShipperId);
        routingOrchestrator.cancelVehicleAssignment(vehicleShipperId);
        return ResponseEntity.ok(GeneralResponse.success("Hủy gán phương tiện cho tài xế thành công"));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<VehicleShipperEntity>>> searchVehicleShipperAssignments(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Long shipperId,
            @RequestParam(required = false) String vehicleId,
            @RequestParam(required = false) LocalDate workingDate) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info(
                "Received request to search vehicle shipper assignments with vehicleId {}, shipperId {}, workingDate {}",
                vehicleId, shipperId, workingDate);
        var result = vehicleShipperService.searchVehicleShippers(shipperId, vehicleId, workingDate, tenantId, page,
                size, sortBy, sortDirection);
        return ResponseEntity.ok(GeneralResponse.success("Tìm kiếm gán phương tiện cho tài xế thành công", PageResponse.of(result)));
    }

    @GetMapping("/search/{vehicleShipperId}")
    public ResponseEntity<GeneralResponse<VehicleShipperEntity>> getVehicleShipperAssignment(
            @PathVariable String vehicleShipperId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to get vehicle shipper assignment for ID {}", vehicleShipperId);
        var result = vehicleShipperService.getDetailedVehicleShipper(vehicleShipperId, tenantId);
        return ResponseEntity
                .ok(GeneralResponse.success("Lấy thông tin gán phương tiện cho tài xế thành công", result));
    }
}
