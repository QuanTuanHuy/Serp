package serp.project.logistics2.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import serp.project.logistics2.dto.request.AddressCreationForm;
import serp.project.logistics2.dto.request.AddressUpdateForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.entity.AddressEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.AddressService;
import serp.project.logistics2.util.AuthUtils;

import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/address")
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final AuthUtils authUtils;

    @GetMapping("/search/by-entity/{entityId}")
    public ResponseEntity<GeneralResponse<List<AddressEntity>>> getAddressesByEntityId(
            @PathVariable("entityId") String entityId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[AddressController] Retrieving addresses for entityId {} and tenantId: {}", entityId, tenantId);
        List<AddressEntity> addresses = addressService.findByEntityId(entityId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Addresses retrieved successfully", addresses));
    }

    @GetMapping("/search/{addressId}")
    public ResponseEntity<GeneralResponse<AddressEntity>> getAddressById(
            @PathVariable("addressId")  String addressId
    ) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[AddressController] Retrieving address for addressId {} and tenantId: {}", addressId, tenantId);
        AddressEntity address = addressService.findById(addressId, tenantId);
        return  ResponseEntity.ok(GeneralResponse.success("Address retrieved successfully", address));
    }

}
