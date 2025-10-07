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
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.core.port.client.IKafkaPublisher;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.service.IContactService;
import serp.project.crm.core.service.ICustomerService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService implements IContactService {

    private final IContactPort contactPort;
    private final IKafkaPublisher kafkaPublisher;
    private final ICustomerService customerService;

    @Override
    @Transactional
    public ContactEntity createContact(ContactEntity contact, Long tenantId) {
        if (contact.getCustomerId() != null) {
            customerService.getCustomerById(contact.getCustomerId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        }

        if (contact.getEmail() != null && !contact.getEmail().matches(Constants.Validation.EMAIL_REGEX)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        contact.setTenantId(tenantId);
        contact.setDefaults();

        ContactEntity saved = contactPort.save(contact);

        publishContactCreatedEvent(saved);

        return saved;
    }

    @Override
    @Transactional
    public ContactEntity updateContact(Long id, ContactEntity updates, Long tenantId) {
        ContactEntity existing = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        existing.updateFrom(updates);

        ContactEntity updated = contactPort.save(existing);

        publishContactUpdatedEvent(updated);

        return updated;
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
    public Pair<List<ContactEntity>, Long> getContactsByCustomer(Long customerId, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();

        // Implement later
        List<ContactEntity> allContacts = contactPort.findByCustomerId(customerId, tenantId);
        int start = pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getSize(), allContacts.size());
        List<ContactEntity> pageContent = allContacts.subList(start, end);

        return Pair.of(pageContent, (long) allContacts.size());
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
    public Pair<List<ContactEntity>, Long> searchContacts(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return contactPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactEntity> getPrimaryContact(Long customerId, Long tenantId) {
        return contactPort.findPrimaryContact(customerId, tenantId);
    }

    @Override
    @Transactional
    public ContactEntity setPrimaryContact(Long id, Long tenantId) {
        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        if (contact.getCustomerId() == null) {
            throw new IllegalStateException("Contact must belong to a customer to be set as primary");
        }

        contactPort.findPrimaryContact(contact.getCustomerId(), tenantId)
                .ifPresent(existing -> {
                    existing.removePrimaryStatus(tenantId);
                    contactPort.save(existing);
                });

        contact.setPrimaryContact(tenantId);
        ContactEntity updated = contactPort.save(contact);

        publishContactUpdatedEvent(updated);

        return updated;
    }

    @Override
    @Transactional
    public void deactivateContact(Long id, Long tenantId) {

        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        contact.deactivate(tenantId);
        contactPort.save(contact);

        publishContactDeletedEvent(contact);

    }

    @Override
    @Transactional
    public void deleteContact(Long id, Long tenantId) {
        ContactEntity contact = contactPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        contactPort.deleteById(id, tenantId);

        publishContactDeletedEvent(contact);

    }

    private void publishContactCreatedEvent(ContactEntity contact) {
        // TODO: Implement event publishing
        log.debug("Event: Contact created - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }

    private void publishContactUpdatedEvent(ContactEntity contact) {
        // TODO: Implement event publishing
        log.debug("Event: Contact updated - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }

    private void publishContactDeletedEvent(ContactEntity contact) {
        // TODO: Implement event publishing
        log.debug("Event: Contact deleted - ID: {}, Topic: {}", contact.getId(), Constants.KafkaTopic.CONTACT);
    }
}
