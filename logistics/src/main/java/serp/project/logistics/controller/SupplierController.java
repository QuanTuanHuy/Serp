package serp.project.logistics.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.logistics.dto.response.GeneralResponse;
import serp.project.logistics.dto.response.PageResponse;
import serp.project.logistics.dto.response.SupplierDetailResponse;
import serp.project.logistics.entity.AddressEntity;
import serp.project.logistics.entity.SupplierEntity;
import serp.project.logistics.exception.AppErrorCode;
import serp.project.logistics.exception.AppException;
import serp.project.logistics.service.AddressService;
import serp.project.logistics.service.SupplierService;
import serp.project.logistics.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("logistics/api/v1/supplier")
@Validated
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;
    private final AuthUtils authUtils;
    private final AddressService addressService;

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<SupplierEntity>>> getSuppliers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String statusId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[SupplierController] Getting suppliers of page {}/{} for tenantId {}", page, size, tenantId);
        Page<SupplierEntity> suppliers = supplierService.findSuppliers(
                query,
                statusId,
                tenantId,
                page,
                size,
                sortBy,
                sortDirection);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get list of suppliers at page " + page,
                PageResponse.of(suppliers)));
    }

    @GetMapping("/search/{supplierId}")
    public ResponseEntity<GeneralResponse<SupplierDetailResponse>> getDetailSupplier(
            @PathVariable("supplierId") String supplierId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[SupplierController] Getting detail supplier with ID {} for tenantId {}", supplierId, tenantId);
        SupplierEntity supplier = supplierService.getSupplier(supplierId, tenantId);
        if (supplier == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        AddressEntity address = addressService.findByEntityId(supplierId, tenantId).stream()
                .filter(AddressEntity::isDefault).findFirst().orElse(null);
        SupplierDetailResponse supplierDetail = SupplierDetailResponse.fromEntity(supplier, address);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get supplier detail", supplierDetail));
    }

}
