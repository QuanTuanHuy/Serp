package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
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
public class AddressController {

    private final AddressService addressService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createAddress(@RequestBody AddressCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        addressService.createAddress(form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Address created successfully"));
    }

    @PatchMapping("/update/{addressId}")
    public ResponseEntity<GeneralResponse<?>> updateAddress(@RequestBody AddressUpdateForm form, @PathVariable("addressId") String addressId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        addressService.updateAddress(addressId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Address updated successfully"));
    }

    @GetMapping("/search/by-entity/{entityId}")
    public ResponseEntity<GeneralResponse<List<AddressEntity>>> getAddressesByEntityId(
            @PathVariable("entityId") String entityId
    ) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        List<AddressEntity> addresses = addressService.findByEntityId(entityId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Addresses retrieved successfully", addresses));
    }

}
