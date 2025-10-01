/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port interface for Customer operations
 * Defines contract between domain layer and infrastructure layer
 */
public interface ICustomerPort {

    /**
     * Save or update customer
     */
    CustomerEntity save(CustomerEntity customerEntity);

    /**
     * Find customer by ID and tenant ID
     */
    Optional<CustomerEntity> findById(Long id, Long tenantId);

    /**
     * Find customer by email and tenant ID
     */
    Optional<CustomerEntity> findByEmail(String email, Long tenantId);

    /**
     * Find all customers by tenant ID with pagination
     * @return Pair of customer list and total count
     */
    Pair<List<CustomerEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    /**
     * Search customers by keyword (name or email) with pagination
     * @return Pair of customer list and total count
     */
    Pair<List<CustomerEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest);

    /**
     * Find customers by parent customer ID
     */
    List<CustomerEntity> findByParentCustomerId(Long parentCustomerId, Long tenantId);

    /**
     * Find customers by active status with pagination
     * @return Pair of customer list and total count
     */
    Pair<List<CustomerEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId, PageRequest pageRequest);

    /**
     * Count customers by active status
     */
    Long countByActiveStatus(ActiveStatus activeStatus, Long tenantId);

    /**
     * Check if customer exists by email and tenant ID
     */
    Boolean existsByEmail(String email, Long tenantId);

    /**
     * Delete customer by ID and tenant ID
     */
    void deleteById(Long id, Long tenantId);

    /**
     * Find top customers by revenue with limit
     */
    List<CustomerEntity> findTopByRevenue(Long tenantId, int limit);

    /**
     * Find customers by industry with pagination
     * @return Pair of customer list and total count
     */
    Pair<List<CustomerEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest);
}
