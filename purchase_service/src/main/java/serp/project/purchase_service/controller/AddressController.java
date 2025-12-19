package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.AddressUpdateForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.entity.AddressEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.AddressService;
import serp.project.purchase_service.util.AuthUtils;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase-service/api/v1/address")
@Validated
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createAddress(@Valid @RequestBody AddressCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[AddressController] Creating address {} for tenantId: {}", form.getFullAddress(), tenantId);
        addressService.createAddress(form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Address created successfully"));
    }

    @PatchMapping("/update/{addressId}")
    public ResponseEntity<GeneralResponse<?>> updateAddress(@Valid @RequestBody AddressUpdateForm form,
            @PathVariable("addressId") String addressId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[AddressController] Updating address {} for tenantId: {}", addressId, tenantId);
        addressService.updateAddress(addressId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Address updated successfully"));
    }

    @GetMapping("/search/by-entity/{entityId}")
    public ResponseEntity<GeneralResponse<List<AddressEntity>>> getAddressesByEntityId(
            @PathVariable("entityId") String entityId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[AddressController] Retrieving addresses for entityId {} and tenantId: {}", entityId, tenantId);
        List<AddressEntity> addresses = addressService.findByEntityId(entityId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Addresses retrieved successfully", addresses));
    }

}
