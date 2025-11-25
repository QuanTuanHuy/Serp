package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.constant.EntityType;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.SupplierCreationForm;
import serp.project.purchase_service.dto.request.SupplierUpdateForm;
import serp.project.purchase_service.entity.SupplierEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.SupplierRepository;
import serp.project.purchase_service.repository.specification.SupplierSpecification;
import serp.project.purchase_service.util.IdUtils;
import serp.project.purchase_service.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressService addressService;

    @Transactional(rollbackFor = Exception.class)
    public void createSupplier(SupplierCreationForm form, Long tenantId) {

        String supplierId = IdUtils.generateSupplierId();
        SupplierEntity supplier = SupplierEntity.builder()
                .id(supplierId)
                .name(form.getName())
                .email(form.getEmail())
                .phone(form.getPhone())
                .statusId(form.getStatusId())
                .tenantId(tenantId)
                .build();
        supplierRepository.save(supplier);
        log.info("[SupplierService] Generated supplier {} with ID {} for tenantId {}", form.getName(), supplierId,
                tenantId);

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(supplierId);
        addressForm.setEntityType(EntityType.SUPPLIER.value());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        addressService.createAddress(addressForm, tenantId);
        log.info("[SupplierService] Created address for supplier ID {} for tenantId {}", supplierId, tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(String id, SupplierUpdateForm form, Long tenantId) {
        SupplierEntity supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null || !supplier.getTenantId().equals(tenantId)) {
            log.error("[SupplierService] Supplier with ID {} not found for tenantId {}", id, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        supplier.setName(form.getName());
        supplier.setEmail(form.getEmail());
        supplier.setPhone(form.getPhone());
        supplier.setStatusId(form.getStatusId());
        supplierRepository.save(supplier);
        log.info("[SupplierService] Updated supplier {} with ID {} for tenantId {}", form.getName(), id, tenantId);
    }

    public Page<SupplierEntity> findSuppliers(
            String query,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return supplierRepository.findAll(SupplierSpecification.satisfy(query, statusId, tenantId), pageable);
    }

    public SupplierEntity getSupplier(String supplierId, Long tenantId) {
        SupplierEntity supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null || !supplier.getTenantId().equals(tenantId)) {
            log.info("[SupplierService] Supplier with ID {} not found for tenantId {}", supplierId, tenantId);
            return null;
        }
        return supplier;
    }

}
