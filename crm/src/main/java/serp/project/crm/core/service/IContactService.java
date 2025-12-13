/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;

import java.util.List;
import java.util.Optional;

public interface IContactService {

    ContactEntity createContact(ContactEntity contact, Long userId, Long tenantId);
    ContactEntity updateContact(Long id, ContactEntity updates, Long userId, Long tenantId);
    
    void deactivateContact(Long id, Long tenantId);
    void deleteContact(Long id, Long tenantId);

    Optional<ContactEntity> getContactById(Long id, Long tenantId);
    Pair<List<ContactEntity>, Long> getAllContacts(Long tenantId, PageRequest pageRequest);
    List<ContactEntity> getContactsByCustomerId(Long customerId, Long tenantId);
    Pair<List<ContactEntity>, Long> getContactsByType(ContactType type, Long tenantId, PageRequest pageRequest);
    Pair<List<ContactEntity>, Long> getContactsByStatus(ActiveStatus status, Long tenantId, PageRequest pageRequest);
    Pair<List<ContactEntity>, Long> searchContacts(String keyword, Long tenantId, PageRequest pageRequest);
    Optional<ContactEntity> getPrimaryContact(Long customerId, Long tenantId);

}
