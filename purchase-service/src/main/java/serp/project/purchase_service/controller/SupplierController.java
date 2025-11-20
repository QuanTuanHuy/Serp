package serp.project.purchase_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import serp.project.purchase_service.dto.request.SupplierCreationForm;
import serp.project.purchase_service.dto.request.SupplierUpdateForm;
import serp.project.purchase_service.dto.response.GeneralResponse;
import serp.project.purchase_service.dto.response.SupplierDetailResponse;
import serp.project.purchase_service.entity.AddressEntity;
import serp.project.purchase_service.entity.SupplierEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.service.AddressService;
import serp.project.purchase_service.service.SupplierService;
import serp.project.purchase_service.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("purchase-service/api/v1/supplier")
@Validated
@Slf4j
public class SupplierController {

    private final SupplierService supplierService;
    private final AuthUtils authUtils;
    private final AddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createSupplier(@RequestBody SupplierCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        supplierService.createSupplier(form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Supplier created successfully"));
    }

    @PatchMapping("/update/{supplierId}")
    public ResponseEntity<GeneralResponse<?>> updateSupplier(@RequestBody SupplierUpdateForm form, @PathVariable("supplierId") String supplierId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        supplierService.updateSupplier(supplierId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Supplier updated successfully"));
    }

    @DeleteMapping("/delete/{supplierId}")
    public ResponseEntity<GeneralResponse<?>> deleteSupplier(@PathVariable("supplierId") String supplierId) {
        throw new AppException(AppErrorCode.UNIMPLEMENTED);
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<Page<SupplierEntity>>> getSuppliers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String statusId
    ) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Page<SupplierEntity> suppliers = supplierService.findSuppliers(
                query,
                statusId,
                tenantId,
                page,
                size,
                sortBy,
                sortDirection
        );
        return ResponseEntity.ok(GeneralResponse.success("Successfully get list of suppliers at page " + page, suppliers));
    }

    @GetMapping("/search/{supplierId}")
    public ResponseEntity<GeneralResponse<SupplierDetailResponse>> getDetailSupplier(@PathVariable("supplierId") String supplierId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        SupplierEntity supplier = supplierService.getSupplier(supplierId, tenantId);
        if (supplier == null) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        AddressEntity address = addressService.findByEntityId(supplierId, tenantId).stream().filter(AddressEntity::isDefault).findFirst().orElse(null);
        SupplierDetailResponse supplierDetail = SupplierDetailResponse.fromEntity(supplier, address);
        return ResponseEntity.ok(GeneralResponse.success("Successfully get supplier detail", supplierDetail));
    }

}
