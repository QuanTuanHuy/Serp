package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressService addressService;

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

        AddressCreationForm addressForm = new AddressCreationForm();
        addressForm.setEntityId(supplierId);
        addressForm.setEntityType(EntityType.SUPPLIER.value());
        addressForm.setAddressType(form.getAddressType());
        addressForm.setLatitude(form.getLatitude());
        addressForm.setLongitude(form.getLongitude());
        addressForm.setDefault(true);
        addressForm.setFullAddress(form.getFullAddress());
        addressService.createAddress(addressForm, tenantId);

    }

    public void updateSupplier(String id, SupplierUpdateForm form, Long tenantId) {
        SupplierEntity supplier = supplierRepository.findById(id).orElse(null);
        if (supplier == null || !supplier.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        supplier.setName(form.getName());
        supplier.setEmail(form.getEmail());
        supplier.setPhone(form.getPhone());
        supplier.setStatusId(form.getStatusId());
        supplierRepository.save(supplier);
    }

    public Page<SupplierEntity> findSuppliers(
            String query,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return supplierRepository.findAll(SupplierSpecification.satisfy(query, statusId, tenantId), pageable);
    }

    public SupplierEntity getSupplier(String supplierId, Long tenantId) {
        SupplierEntity supplier = supplierRepository.findById(supplierId).orElse(null);
        if (supplier == null || !supplier.getTenantId().equals(tenantId)) {
            return null;
        }
        return supplier;
    }

}
