package serp.project.logistics2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics2.dto.request.FacilityCreationForm;
import serp.project.logistics2.dto.request.FacilityUpdateForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.AddressEntity;
import serp.project.logistics2.entity.FacilityEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.AddressService;
import serp.project.logistics2.service.FacilityService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/facility")
@Validated
@Slf4j
public class FacilityController {

    private final FacilityService facilityService;
    private final AddressService addressService;
    private final AuthUtils authUtils;

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
