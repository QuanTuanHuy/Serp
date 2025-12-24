/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.ICustomerPort;
import serp.project.crm.core.service.ICustomerService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final ICustomerPort customerPort;

    @Transactional
    public CustomerEntity createCustomer(CustomerEntity customer, Long tenantId) {
        if (customerPort.existsByEmail(customer.getEmail(), tenantId)) {
            throw new AppException(ErrorMessage.CUSTOMER_ALREADY_EXISTS);
        }

        if (customer.getParentCustomerId() != null) {
            customerPort.findById(customer.getParentCustomerId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));
        }

        customer.setTenantId(tenantId);
        customer.setDefaults();

        CustomerEntity saved = customerPort.save(customer);

        publishCustomerCreatedEvent(saved);

        return saved;
    }

    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity updates, Long tenantId) {
        CustomerEntity existing = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));

        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (customerPort.existsByEmail(updates.getEmail(), tenantId)) {
                throw new AppException(ErrorMessage.CUSTOMER_ALREADY_EXISTS);
            }
        }

        if (updates.getParentCustomerId() != null
                && !updates.getParentCustomerId().equals(existing.getParentCustomerId())) {
            if (updates.getParentCustomerId().equals(id)) {
                throw new AppException(ErrorMessage.CUSTOMER_CANNOT_BE_OWN_PARENT);
            }
            customerPort.findById(updates.getParentCustomerId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));
        }

        existing.updateFrom(updates);

        CustomerEntity updated = customerPort.save(existing);

        publishCustomerUpdatedEvent(updated);

        return updated;
    }

    @Transactional(readOnly = true)
    public Optional<CustomerEntity> getCustomerById(Long id, Long tenantId) {
        return customerPort.findById(id, tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerEntity> getCustomerByEmail(String email, Long tenantId) {
        return customerPort.findByEmail(email, tenantId);
    }

    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getAllCustomers(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findAll(tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> searchCustomers(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> getChildCustomers(Long parentId, Long tenantId) {
        customerPort.findById(parentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Parent customer not found"));

        return customerPort.findByParentCustomerId(parentId, tenantId);
    }

    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getCustomersByStatus(ActiveStatus status, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findByActiveStatus(status, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<CustomerEntity> getTopCustomersByRevenue(Long tenantId, int limit) {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        return customerPort.findTopByRevenue(tenantId, limit);
    }

    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getCustomersByIndustry(String industry, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findByIndustry(industry, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Long countCustomersByStatus(ActiveStatus status, Long tenantId) {
        return customerPort.countByActiveStatus(status, tenantId);
    }

    @Transactional
    public void deactivateCustomer(Long id, Long tenantId) {
        CustomerEntity customer = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // TODO: Check if customer has active opportunities before deactivating

        customer.setActiveStatus(ActiveStatus.INACTIVE);
        customerPort.save(customer);

        publishCustomerDeletedEvent(customer);
    }

    @Transactional
    public void deleteCustomer(Long id, Long tenantId) {
        CustomerEntity customer = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));

        List<CustomerEntity> children = customerPort.findByParentCustomerId(id, tenantId);
        if (!children.isEmpty()) {
            throw new AppException(ErrorMessage.CANNOT_DELETE_CUSTOMER_WITH_CHILDREN);
        }

        // TODO: Validation: No active opportunities

        customerPort.deleteById(id, tenantId);

        // Publish event
        publishCustomerDeletedEvent(customer);

    }

    @Transactional
    public void updateCustomerRevenue(Long customerId, Long tenantId, BigDecimal revenue, boolean isWon) {

        CustomerEntity customer = customerPort.findById(customerId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customer.recordOpportunityResult(isWon, revenue != null ? revenue : BigDecimal.ZERO, tenantId);

        customerPort.save(customer);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email, Long tenantId) {
        return customerPort.existsByEmail(email, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> filterCustomers(CustomerFilterRequest filter, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.filter(filter, pageRequest, tenantId);
    }

    private void publishCustomerCreatedEvent(CustomerEntity customer) {
        log.debug("Event: Customer created - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }

    private void publishCustomerUpdatedEvent(CustomerEntity customer) {
        log.debug("Event: Customer updated - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }

    private void publishCustomerDeletedEvent(CustomerEntity customer) {
        log.debug("Event: Customer deleted - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }
}
