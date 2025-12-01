package serp.project.logistics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics.entity.SupplierEntity;
import serp.project.logistics.repository.SupplierRepository;
import serp.project.logistics.repository.specification.SupplierSpecification;
import serp.project.logistics.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierService {

    private final SupplierRepository supplierRepository;

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
