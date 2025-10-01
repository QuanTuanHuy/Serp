/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.infrastructure.store.mapper.ContactMapper;
import serp.project.crm.infrastructure.store.repository.ContactRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContactAdapter implements IContactPort {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @Override
    public ContactEntity save(ContactEntity contactEntity) {
        var model = contactMapper.toModel(contactEntity);
        var savedModel = contactRepository.save(model);
        return contactMapper.toEntity(savedModel);
    }

    @Override
    public Optional<ContactEntity> findById(Long id, Long tenantId) {
        return contactRepository.findByIdAndTenantId(id, tenantId)
                .map(contactMapper::toEntity);
    }

    @Override
    public Optional<ContactEntity> findByEmail(String email, Long tenantId) {
        return contactRepository.findByEmailAndTenantId(email, tenantId)
                .map(contactMapper::toEntity);
    }

    @Override
    public Pair<List<ContactEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = contactMapper.toPageable(pageRequest);
        var page = contactRepository.findByTenantId(tenantId, pageable)
                .map(contactMapper::toEntity);
        return contactMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ContactEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest) {
        var pageable = contactMapper.toPageable(pageRequest);
        var page = contactRepository.searchByKeyword(tenantId, keyword, pageable)
                .map(contactMapper::toEntity);
        return contactMapper.pageToPair(page);
    }

    @Override
    public List<ContactEntity> findByCustomerId(Long customerId, Long tenantId) {
        return contactRepository.findByTenantIdAndCustomerId(tenantId, customerId)
                .stream()
                .map(contactMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ContactEntity> findPrimaryContact(Long customerId, Long tenantId) {
        return contactRepository.findByTenantIdAndCustomerIdAndIsPrimary(tenantId, customerId, true)
                .map(contactMapper::toEntity);
    }

    @Override
    public Pair<List<ContactEntity>, Long> findByContactType(ContactType contactType, Long tenantId,
            PageRequest pageRequest) {
        var pageable = contactMapper.toPageable(pageRequest);
        var page = contactRepository.findByTenantIdAndContactType(tenantId, contactType.name(), pageable)
                .map(contactMapper::toEntity);
        return contactMapper.pageToPair(page);
    }

    @Override
    public Pair<List<ContactEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest) {
        var pageable = contactMapper.toPageable(pageRequest);
        var page = contactRepository.findByTenantIdAndActiveStatus(tenantId, activeStatus.name(), pageable)
                .map(contactMapper::toEntity);
        return contactMapper.pageToPair(page);
    }

    @Override
    public Long countByCustomerId(Long customerId, Long tenantId) {
        return contactRepository.countByTenantIdAndCustomerId(tenantId, customerId);
    }

    @Override
    public Boolean existsByEmail(String email, Long tenantId) {
        return contactRepository.existsByEmailAndTenantId(email, tenantId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        contactRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(contactRepository::delete);
    }
}
