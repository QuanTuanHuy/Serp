package serp.project.logistics2.controller;

import org.springframework.data.domain.Page;
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
import serp.project.logistics2.dto.request.VehicleCreationForm;
import serp.project.logistics2.dto.request.VehicleUpdateForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.VehicleEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.VehicleService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/vehicles")
@Validated
@Slf4j
public class VehicleController {

        private final VehicleService vehicleService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createVehicle(
                        @Valid @RequestBody VehicleCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[VehicleController] Create vehicle by tenant id {}", tenantId);
                vehicleService.createVehicle(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Tạo phương tiện thành công"));
        }

        @PutMapping("/update/{vehicleId}")
        public ResponseEntity<GeneralResponse<?>> updateVehicle(
                        @Valid @RequestBody VehicleUpdateForm form,
                        @PathVariable String vehicleId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[VehicleController] Update vehicle by tenant id {}", tenantId);
                vehicleService.updateVehicle(vehicleId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Cập nhật phương tiện thành công"));
        }

        @PutMapping("/update/active/{vehicleId}")
        public ResponseEntity<GeneralResponse<?>> updateVehicleActiveStatus(
                        @PathVariable String vehicleId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[VehicleController] Update vehicle active status by tenant id {}", tenantId);
                vehicleService.activateVehicle(vehicleId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Cập nhật trạng thái phương tiện thành công"));
        }

        @PutMapping("/update/deactive/{vehicleId}")
        public ResponseEntity<GeneralResponse<?>> updateVehicleInactiveStatus(
                        @PathVariable String vehicleId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[VehicleController] Update vehicle inactive status by tenant id {}", tenantId);
                vehicleService.deactivateVehicle(vehicleId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Cập nhật trạng thái phương tiện thành công"));
        }

        @GetMapping("/search/{vehicleId}")
        public ResponseEntity<GeneralResponse<VehicleEntity>> getVehicleDetail(
                        @PathVariable String vehicleId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[VehicleController] Get vehicle detail by tenant id {}", tenantId);
                VehicleEntity vehicle = vehicleService.getVehicleById(vehicleId, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Lấy thông tin phương tiện thành công", vehicle));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<VehicleEntity>>> searchVehicles(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String vehicleType,
                        @RequestParam(required = false) String vehicleStatus) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info(
                                "[VehicleController] Search vehicles by tenant id {}, query {}, vehicleType {}, vehicleStatus {}, page {}, size {}, sortBy {}, sortDirection {}",
                                tenantId, query, vehicleType, vehicleStatus, page, size, sortBy, sortDirection);
                Page<VehicleEntity> vehicles = vehicleService.searchVehicles(query, vehicleType, vehicleStatus,
                                tenantId, page,
                                size, sortBy, sortDirection);
                return ResponseEntity.ok(GeneralResponse.success("Tìm kiếm phương tiện thành công", PageResponse.of(vehicles)));
        }
}