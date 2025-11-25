package serp.project.purchase_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
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
        log.info("[AddressService] Created address {} with ID {} for tenantId: {}", address.getFullAddress(), addressId,
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
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
        log.info("[AddressService] Updated address {} with ID {} for tenantId: {}", address.getFullAddress(), addressId,
                tenantId);
    }

    public List<AddressEntity> findByEntityId(String entityId, Long tenantId) {
        return addressRepository.findByTenantIdAndEntityId(tenantId, entityId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(String addressId, Long tenantId) {
        AddressEntity address = addressRepository.findById(addressId).orElse(null);
        if (address == null || !address.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        addressRepository.delete(address);
        log.info("[AddressService] Deleted address {} with ID {} for tenantId: {}", address.getFullAddress(), addressId,
                tenantId);
    }

}
