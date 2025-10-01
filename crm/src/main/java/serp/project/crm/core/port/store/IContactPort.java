/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;

import java.util.List;
import java.util.Optional;

public interface IContactPort {

    ContactEntity save(ContactEntity contactEntity);

    Optional<ContactEntity> findById(Long id, Long tenantId);

    Optional<ContactEntity> findByEmail(String email, Long tenantId);

    Pair<List<ContactEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<ContactEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest);

    List<ContactEntity> findByCustomerId(Long customerId, Long tenantId);

    Optional<ContactEntity> findPrimaryContact(Long customerId, Long tenantId);

    Pair<List<ContactEntity>, Long> findByContactType(ContactType contactType, Long tenantId, PageRequest pageRequest);

    Pair<List<ContactEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest);

    Long countByCustomerId(Long customerId, Long tenantId);

    Boolean existsByEmail(String email, Long tenantId);

    void deleteById(Long id, Long tenantId);

}
