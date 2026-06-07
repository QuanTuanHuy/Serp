/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.service.IContactService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService implements IContactService {

    private final IContactPort contactPort;
    private final IAccountPort accountPort;

    @Override
    @Transactional
    public ContactEntity createContact(ContactEntity contact, Long userId, Long tenantId) {
        if (contact.getAccountId() != null) {
            accountPort.findById(contact.getAccountId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        }

        contact.setTenantId(tenantId);
        contact.setCreatedBy(userId);
        contact.setDefaults();

        ContactEntity saved = contactPort.save(contact);

        publishContactCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public ContactEntity updateContact(Long id, ContactEntity updates, Long userId, Long tenantId) {
        ContactEntity existing = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CONTACT_NOT_FOUND));

        existing.updateFrom(updates);
        existing.setUpdatedBy(userId);
        if (updates.getIsPrimary() != null && updates.getIsPrimary() && !existing.getIsPrimary()) {
            setPrimaryContact(existing, userId, tenantId);
        }

        ContactEntity updated = contactPort.save(existing);

        publishContactUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional
    public ContactEntity setPrimaryContact(Long id, Long userId, Long tenantId) {
        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CONTACT_NOT_FOUND));
        setPrimaryContact(contact, userId, tenantId);
        ContactEntity updated = contactPort.save(contact);
        publishContactUpdatedEvent(updated);
        return updated;
    }

    private void setPrimaryContact(ContactEntity contact, Long userId, Long tenantId) {
        if (contact.getAccountId() == null) {
            throw new AppException(ErrorMessage.CONTACT_CANNOT_BE_PRIMARY);
        }
        if (!contact.isActive()) {
            throw new AppException("Cannot set inactive contact as primary");
        }

        contactPort.findPrimaryContact(contact.getAccountId(), tenantId)
                .ifPresent(existing -> {
                    existing.removePrimaryStatus(userId);
                    contactPort.save(existing);
                });

        contact.setPrimaryContact(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactEntity> getContactById(Long id, Long tenantId) {
        return contactPort.findById(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ContactEntity>, Long> getAllContacts(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return contactPort.findAll(tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactEntity> getContactsByAccountId(Long accountId, Long tenantId) {
        return contactPort.findByAccountId(accountId, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ContactEntity>, Long> getContactsByType(ContactType type, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return contactPort.findByContactType(type, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ContactEntity>, Long> getContactsByStatus(ActiveStatus status, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return contactPort.findByActiveStatus(status, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactEntity> getContactsByIds(List<Long> ids, Long tenantId) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return contactPort.findByIds(ids, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ContactEntity>, Long> searchContacts(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return contactPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactEntity> getPrimaryContact(Long accountId, Long tenantId) {
        return contactPort.findPrimaryContact(accountId, tenantId);
    }

    @Override
    @Transactional
    public ContactEntity deactivateContact(Long id, Long userId, Long tenantId) {

        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CONTACT_NOT_FOUND));

        contact.deactivate(userId);
        ContactEntity updated = contactPort.save(contact);

        publishContactUpdatedEvent(updated);
        return updated;

    }

    @Override
    @Transactional
    public void deleteContact(Long id, Long tenantId) {
        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CONTACT_NOT_FOUND));

        contactPort.deleteById(id, tenantId);

        publishContactDeletedEvent(contact);

    }

    private void publishContactCreatedEvent(ContactEntity contact) {
        log.debug("Event: Contact created - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }

    private void publishContactUpdatedEvent(ContactEntity contact) {
        log.debug("Event: Contact updated - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }

    private void publishContactDeletedEvent(ContactEntity contact) {
        log.debug("Event: Contact deleted - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }
}
