package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.purchase_service.dto.request.SupplierCreationForm;
import serp.project.purchase_service.dto.request.SupplierUpdateForm;
import serp.project.purchase_service.entity.SupplierEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.AddressRepository;
import serp.project.purchase_service.repository.SupplierRepository;
import serp.project.purchase_service.repository.specification.SupplierSpecification;
import serp.project.purchase_service.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createSupplier(SupplierCreationForm form, Long tenantId) {

        SupplierEntity supplier = new SupplierEntity(form, tenantId);
        supplierRepository.save(supplier);
        log.info("[SupplierService] Generated supplier {} with ID {} for tenantId {}", form.getName(), supplier.getId(),
                tenantId);

        addressRepository.save(supplier.getAddress());
        log.info("[SupplierService] Created address for supplier ID {} for tenantId {}", supplier.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSupplier(String id, SupplierUpdateForm form, Long tenantId) {
        SupplierEntity supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null || !supplier.getTenantId().equals(tenantId)) {
            log.error("[SupplierService] Supplier with ID {} not found for tenantId {}", id, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        supplier.update(form);
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
