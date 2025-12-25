package serp.project.logistics.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics.dto.request.FacilityCreationForm;
import serp.project.logistics.dto.request.FacilityUpdateForm;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.entity.AddressEntity;
import serp.project.logistics.entity.FacilityEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.AddressService;
import serp.project.logistics.service.FacilityService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics/api/v1/facility")
@Validated
@Slf4j
public class FacilityController {

        private final FacilityService facilityService;
        private final AddressService addressService;
        private final AuthUtils authUtils;

        @PostMapping("/create")
        public ResponseEntity<GeneralResponse<?>> createFacility(@Valid @RequestBody FacilityCreationForm form) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[FacilityController] Creating facility {} for tenantId: {}", form.getName(), tenantId);
                facilityService.createFacility(form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Facility created successfully"));
        }

        @PatchMapping("/update/{facilityId}")
        public ResponseEntity<GeneralResponse<?>> updateFacility(@Valid @RequestBody FacilityUpdateForm form,
                        @PathVariable("facilityId") String facilityId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[FacilityController] Updating facility {} with ID {} for tenantId: {}", form.getName(),
                                facilityId, tenantId);
                facilityService.updateFacility(facilityId, form, tenantId);
                return ResponseEntity.ok(GeneralResponse.success("Facility updated successfully"));
        }

        @GetMapping("/search")
        public ResponseEntity<GeneralResponse<PageResponse<FacilityEntity>>> getFacilities(
                        @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
                        @RequestParam(required = false, defaultValue = "10") int size,
                        @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
                        @RequestParam(required = false, defaultValue = "desc") String sortDirection,
                        @RequestParam(required = false) String query,
                        @RequestParam(required = false) String statusId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                Page<FacilityEntity> facilities = facilityService.findFacilities(
                                query,
                                statusId,
                                tenantId,
                                page,
                                size,
                                sortBy,
                                sortDirection);
                log.info("[FacilityController] Retrieved list of facilities for tenantId: {} on page {}/{}", tenantId,
                                page, size);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get list of facility page " + page,
                                PageResponse.of(facilities)));
        }

        @DeleteMapping("/delete/{facilityId}")
        public ResponseEntity<GeneralResponse<?>> deleteFacility(@PathVariable("facilityId") String facilityId) {
                // Long tenantId = authUtils.getCurrentTenantId()
                // .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                // facilityService.deleteFacility(facilityId, tenantId);
                // return ResponseEntity.ok(GeneralResponse.success("Facility deleted
                // successfully"));
                throw new AppException(AppErrorCode.UNIMPLEMENTED);
        }

        @GetMapping("/search/{facilityId}")
        public ResponseEntity<GeneralResponse<FacilityEntity>> getFacilityDetail(
                        @PathVariable("facilityId") String facilityId) {
                Long tenantId = authUtils.getCurrentTenantId()
                                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
                log.info("[FacilityController] Retrieving facility detail for facilityId {} and tenantId: {}",
                                facilityId, tenantId);
                FacilityEntity facility = facilityService.getFacility(facilityId, tenantId);
                if (facility == null) {
                        throw new AppException(AppErrorCode.NOT_FOUND);
                }
                AddressEntity address = addressService.findByEntityId(facilityId, tenantId).stream().findFirst()
                                .orElse(null);
                facility.setAddress(address);
                return ResponseEntity.ok(GeneralResponse.success("Successfully get facility detail", facility));
        }

}
