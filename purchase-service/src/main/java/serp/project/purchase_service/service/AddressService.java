package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.purchase_service.dto.request.AddressCreationForm;
import serp.project.purchase_service.dto.request.AddressUpdateForm;
import serp.project.purchase_service.entity.AddressEntity;
import serp.project.purchase_service.exception.AppErrorCode;
import serp.project.purchase_service.exception.AppException;
import serp.project.purchase_service.repository.AddressRepository;
import serp.project.purchase_service.util.IdUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    public void createAddress(AddressCreationForm form, Long tenantId) {
        String addressId = IdUtils.generateAddressId();
        AddressEntity address = AddressEntity.builder()
                .id(addressId)
                .entityId(form.getEntityId())
                .entityType(form.getEntityType())
                .addressType(form.getAddressType())
                .latitude(form.getLatitude())
                .longitude(form.getLongitude())
                .isDefault(form.isDefault())
                .fullAddress(form.getFullAddress())
                .tenantId(tenantId)
                .build();
        addressRepository.save(address);
    }

    public void updateAddress(String addressId, AddressUpdateForm form, Long tenantId) {
        AddressEntity address = addressRepository.findById(addressId).orElse(null);
        if (address == null || !address.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        address.setAddressType(form.getAddressType());
        address.setLatitude(form.getLatitude());
        address.setLongitude(form.getLongitude());
        address.setDefault(form.isDefault());
        address.setFullAddress(form.getFullAddress());
        addressRepository.save(address);
    }

    public List<AddressEntity> findByEntityId(String entityId, Long tenantId) {
        return addressRepository.findByEntityIdAndTenantId(entityId, tenantId);
    }

    public void deleteAddress(String addressId, Long tenantId) {
        AddressEntity address = addressRepository.findById(addressId).orElse(null);
        if (address == null || !address.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        addressRepository.delete(address);
    }

}
