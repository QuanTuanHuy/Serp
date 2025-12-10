/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.util.List;
import java.util.Optional;

public interface ICustomerPort {
    CustomerEntity save(CustomerEntity customerEntity);

    Optional<CustomerEntity> findById(Long id, Long tenantId);

    Optional<CustomerEntity> findByEmail(String email, Long tenantId);

    Pair<List<CustomerEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<CustomerEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest);

    List<CustomerEntity> findByParentCustomerId(Long parentCustomerId, Long tenantId);

    Pair<List<CustomerEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest);

    Long countByActiveStatus(ActiveStatus activeStatus, Long tenantId);

    Boolean existsByEmail(String email, Long tenantId);

    void deleteById(Long id, Long tenantId);

    List<CustomerEntity> findTopByRevenue(Long tenantId, int limit);

    Pair<List<CustomerEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest);

    Pair<List<CustomerEntity>, Long> filter(CustomerFilterRequest filter, PageRequest pageRequest, Long tenantId);
}
