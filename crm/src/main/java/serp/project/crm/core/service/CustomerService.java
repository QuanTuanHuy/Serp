/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.port.store.ICustomerPort;
import serp.project.crm.core.port.client.IKafkaPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Customer Service - Business logic for customer management
 * Responsibilities:
 * - CRUD operations with business validation
 * - Customer hierarchy management
 * - Revenue calculation and aggregation
 * - Event publishing for customer lifecycle
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final ICustomerPort customerPort;
    private final IKafkaPublisher kafkaPublisher;

    /**
     * Create new customer with validation
     * Business rules:
     * - Email must be unique within tenant
     * - Parent customer must exist if specified
     * - Active status defaults to ACTIVE
     */
    @Transactional
    public CustomerEntity createCustomer(CustomerEntity customer, Long tenantId) {
        log.info("Creating customer with email {} for tenant {}", customer.getEmail(), tenantId);

        // Validation: Check duplicate email
        if (customerPort.existsByEmail(customer.getEmail(), tenantId)) {
            throw new IllegalArgumentException("Customer with email " + customer.getEmail() + " already exists");
        }

        // Validation: Parent customer exists
        if (customer.getParentCustomerId() != null) {
            customerPort.findById(customer.getParentCustomerId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent customer not found"));
        }

        // Set defaults
        customer.setTenantId(tenantId);
        customer.setDefaults();

        // Save to database
        CustomerEntity saved = customerPort.save(customer);

        // Publish event
        publishCustomerCreatedEvent(saved);

        log.info("Customer created successfully with ID {}", saved.getId());
        return saved;
    }

    /**
     * Update existing customer
     * Business rules:
     * - Customer must exist
     * - Cannot change email to existing one
     * - Parent customer validation
     */
    @Transactional
    public CustomerEntity updateCustomer(Long id, CustomerEntity updates, Long tenantId) {
        log.info("Updating customer {} for tenant {}", id, tenantId);

        // Fetch existing customer
        CustomerEntity existing = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validation: Email uniqueness if changed
        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (customerPort.existsByEmail(updates.getEmail(), tenantId)) {
                throw new IllegalArgumentException("Customer with email " + updates.getEmail() + " already exists");
            }
        }

        // Validation: Parent customer
        if (updates.getParentCustomerId() != null && !updates.getParentCustomerId().equals(existing.getParentCustomerId())) {
            if (updates.getParentCustomerId().equals(id)) {
                throw new IllegalArgumentException("Customer cannot be its own parent");
            }
            customerPort.findById(updates.getParentCustomerId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent customer not found"));
        }

        // Use entity method for update
        existing.updateFrom(updates);

        // Save
        CustomerEntity updated = customerPort.save(existing);

        // Publish event
        publishCustomerUpdatedEvent(updated);

        log.info("Customer {} updated successfully", id);
        return updated;
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public Optional<CustomerEntity> getCustomerById(Long id, Long tenantId) {
        return customerPort.findById(id, tenantId);
    }

    /**
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public Optional<CustomerEntity> getCustomerByEmail(String email, Long tenantId) {
        return customerPort.findByEmail(email, tenantId);
    }

    /**
     * Get all customers with pagination
     */
    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getAllCustomers(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findAll(tenantId, pageRequest);
    }

    /**
     * Search customers by keyword
     */
    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> searchCustomers(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    /**
     * Get child customers (hierarchy)
     */
    @Transactional(readOnly = true)
    public List<CustomerEntity> getChildCustomers(Long parentId, Long tenantId) {
        // Validate parent exists
        customerPort.findById(parentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Parent customer not found"));
        
        return customerPort.findByParentCustomerId(parentId, tenantId);
    }

    /**
     * Get customers by active status with pagination
     */
    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getCustomersByStatus(ActiveStatus status, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findByActiveStatus(status, tenantId, pageRequest);
    }

    /**
     * Get top customers by revenue
     */
    @Transactional(readOnly = true)
    public List<CustomerEntity> getTopCustomersByRevenue(Long tenantId, int limit) {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        return customerPort.findTopByRevenue(tenantId, limit);
    }

    /**
     * Get customers by industry
     */
    @Transactional(readOnly = true)
    public Pair<List<CustomerEntity>, Long> getCustomersByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return customerPort.findByIndustry(industry, tenantId, pageRequest);
    }

    /**
     * Count customers by status
     */
    @Transactional(readOnly = true)
    public Long countCustomersByStatus(ActiveStatus status, Long tenantId) {
        return customerPort.countByActiveStatus(status, tenantId);
    }

    /**
     * Deactivate customer (soft delete)
     * Business rules:
     * - Customer must exist
     * - Set status to INACTIVE
     * - Cannot deactivate if has active opportunities
     */
    @Transactional
    public void deactivateCustomer(Long id, Long tenantId) {
        log.info("Deactivating customer {} for tenant {}", id, tenantId);

        CustomerEntity customer = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // TODO: Check if customer has active opportunities before deactivating

        customer.setActiveStatus(ActiveStatus.INACTIVE);
        customerPort.save(customer);

        // Publish event
        publishCustomerDeletedEvent(customer);

        log.info("Customer {} deactivated successfully", id);
    }

    /**
     * Hard delete customer
     * Business rules:
     * - Customer must exist
     * - Must not have child customers
     * - Must not have active opportunities
     */
    @Transactional
    public void deleteCustomer(Long id, Long tenantId) {
        log.info("Deleting customer {} for tenant {}", id, tenantId);

        CustomerEntity customer = customerPort.findById(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validation: No child customers
        List<CustomerEntity> children = customerPort.findByParentCustomerId(id, tenantId);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with child customers");
        }

        // TODO: Validation: No active opportunities

        customerPort.deleteById(id, tenantId);

        // Publish event
        publishCustomerDeletedEvent(customer);

        log.info("Customer {} deleted successfully", id);
    }

    /**
     * Update customer revenue statistics
     * Called when opportunity is won
     */
    @Transactional
    public void updateCustomerRevenue(Long customerId, Long tenantId, BigDecimal revenue, boolean isWon) {
        log.info("Updating revenue for customer {} : {} (won: {})", customerId, revenue, isWon);

        CustomerEntity customer = customerPort.findById(customerId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Use entity method
        customer.recordOpportunityResult(isWon, revenue != null ? revenue : BigDecimal.ZERO, tenantId);

        customerPort.save(customer);

        log.info("Customer {} revenue updated successfully", customerId);
    }

    // ========== Event Publishing ==========

    private void publishCustomerCreatedEvent(CustomerEntity customer) {
        // TODO: Implement event publishing
        log.debug("Event: Customer created - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }

    private void publishCustomerUpdatedEvent(CustomerEntity customer) {
        // TODO: Implement event publishing
        log.debug("Event: Customer updated - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }

    private void publishCustomerDeletedEvent(CustomerEntity customer) {
        // TODO: Implement event publishing
        log.debug("Event: Customer deleted - ID: {}, Topic: {}", customer.getId(), Constants.KafkaTopic.CUSTOMER);
    }
}
