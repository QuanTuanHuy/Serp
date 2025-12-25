package serp.project.sales.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.sales.dto.request.AddressCreationForm;
import serp.project.sales.dto.request.AddressUpdateForm;
import serp.project.sales.entity.AddressEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.repository.AddressRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createAddress(AddressCreationForm form, Long tenantId) {
        AddressEntity address = new AddressEntity(form, tenantId);
        addressRepository.save(address);
        log.info("[AddressService] Created address {} with ID {} for tenantId: {}", address.getFullAddress(),
                address.getId(),
                tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(String addressId, AddressUpdateForm form, Long tenantId) {
        AddressEntity address = addressRepository.findById(addressId).orElse(null);
        if (address == null || !address.getTenantId().equals(tenantId)) {
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        address.update(form);
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
