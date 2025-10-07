/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ICustomerService {

    CustomerEntity createCustomer(CustomerEntity customer, Long tenantId);

    CustomerEntity updateCustomer(Long id, CustomerEntity updates, Long tenantId);

    Optional<CustomerEntity> getCustomerById(Long id, Long tenantId);

    Optional<CustomerEntity> getCustomerByEmail(String email, Long tenantId);

    Pair<List<CustomerEntity>, Long> getAllCustomers(Long tenantId, PageRequest pageRequest);

    Pair<List<CustomerEntity>, Long> searchCustomers(String keyword, Long tenantId, PageRequest pageRequest);

    List<CustomerEntity> getChildCustomers(Long parentId, Long tenantId);

    Pair<List<CustomerEntity>, Long> getCustomersByStatus(ActiveStatus status, Long tenantId, PageRequest pageRequest);

    List<CustomerEntity> getTopCustomersByRevenue(Long tenantId, int limit);

    Pair<List<CustomerEntity>, Long> getCustomersByIndustry(String industry, Long tenantId, PageRequest pageRequest);

    Long countCustomersByStatus(ActiveStatus status, Long tenantId);

    void deactivateCustomer(Long id, Long tenantId);

    void deleteCustomer(Long id, Long tenantId);

    void updateCustomerRevenue(Long customerId, Long tenantId, BigDecimal revenue, boolean isWon);
}
