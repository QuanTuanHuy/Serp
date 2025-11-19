package serp.project.purchase_service.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.purchase_service.dto.request.FacilityCreationForm;
import serp.project.purchase_service.dto.request.FacilityUpdateForm;
import serp.project.purchase_service.dto.response.FacilityDetailResponse;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.PageResponse;
import serp.project.purchase_service.entity.AddressEntity;
import serp.project.purchase_service.entity.FacilityEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.AddressService;
import serp.project.purchase_service.service.FacilityService;
import serp.project.purchase_service.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-service/api/v1/facility")
@Validated
public class FacilityController {

    private final FacilityService facilityService;
    private final AddressService addressService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createFacility(@RequestBody FacilityCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        facilityService.createFacility(form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Facility created successfully"));
    }

    @PatchMapping("/update/{facilityId}")
    public ResponseEntity<GeneralResponse<?>> updateFacility(@RequestBody FacilityUpdateForm form, @PathVariable("facilityId") String facilityId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        facilityService.updateFacility(facilityId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Facility updated successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<FacilityEntity>>> getFacilities(
            @Min(1) @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String statusId
    ) {
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
        return ResponseEntity.ok(GeneralResponse.success("Successfully get list of facility page " + page, PageResponse.of(facilities)));
    }

    @DeleteMapping("/delete/{facilityId}")
    public ResponseEntity<GeneralResponse<?>> deleteFacility(@PathVariable("facilityId") String facilityId) {
//        Long tenantId = authUtils.getCurrentTenantId()
//                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
//        facilityService.deleteFacility(facilityId, tenantId);
//        return ResponseEntity.ok(GeneralResponse.success("Facility deleted successfully"));
        throw new AppException(AppErrorCode.UNIMPLEMENTED);
    }

    @GetMapping("/search/{facilityId}")
    public ResponseEntity<GeneralResponse<FacilityDetailResponse>> getFacilityDetail(@PathVariable("facilityId") String facilityId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        FacilityEntity facility = facilityService.getFacility(facilityId, tenantId);
        if (facility == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        AddressEntity address = addressService.findByEntityId(facilityId, tenantId).stream().findFirst().orElse(null);
        FacilityDetailResponse response = FacilityDetailResponse.fromEntity(
                facility,
                address
        );
        return ResponseEntity.ok(GeneralResponse.success("Successfully get facility detail", response));
    }

}
