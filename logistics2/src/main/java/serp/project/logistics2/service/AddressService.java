package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.logistics2.entity.AddressEntity;
import serp.project.logistics2.repository.AddressRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    public List<AddressEntity> findByEntityId(String entityId, Long tenantId) {
        return addressRepository.findByTenantIdAndEntityId(tenantId, entityId);
    }

    public AddressEntity findById(String id, Long tenantId) {
        return addressRepository.findById(id).filter(a -> a.getTenantId().equals(tenantId)).orElse(null);
    }

}
