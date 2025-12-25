package serp.project.sales.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.sales.dto.request.CustomerCreationForm;
import serp.project.sales.dto.request.CustomerUpdateForm;
import serp.project.sales.entity.CustomerEntity;
import serp.project.sales.exception.AppErrorCode;
import serp.project.sales.exception.AppException;
import serp.project.sales.repository.AddressRepository;
import serp.project.sales.repository.CustomerRepository;
import serp.project.sales.repository.specification.CustomerSpecification;
import serp.project.sales.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    @Transactional(rollbackFor = Exception.class)
    public void createCustomer(CustomerCreationForm form, Long tenantId) {

        CustomerEntity customer = new CustomerEntity(form, tenantId);
        customerRepository.save(customer);
        log.info("[CustomerService] Generated customer {} with ID {} for tenantId {}", form.getName(), customer.getId(),
                tenantId);

        addressRepository.save(customer.getAddress());
        log.info("[CustomerService] Created address for customer ID {} for tenantId {}", customer.getId(), tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCustomer(String id, CustomerUpdateForm form, Long tenantId) {
        CustomerEntity customer = customerRepository.findById(id).orElse(null);
        if (customer == null || !customer.getTenantId().equals(tenantId)) {
            log.error("[CustomerService] Customer with ID {} not found for tenantId {}", id, tenantId);
            throw new AppException(AppErrorCode.NOT_FOUND);
        }
        customer.update(form);
        customerRepository.save(customer);
        log.info("[CustomerService] Updated customer {} with ID {} for tenantId {}", form.getName(), id, tenantId);
    }

    public Page<CustomerEntity> findCustomers(
            String query,
            String statusId,
            Long tenantId,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, sortDirection);
        return customerRepository.findAll(CustomerSpecification.satisfy(query, statusId, tenantId), pageable);
    }

    public CustomerEntity getCustomer(String customerId, Long tenantId) {
        CustomerEntity customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null || !customer.getTenantId().equals(tenantId)) {
            log.info("[CustomerService] Customer with ID {} not found for tenantId {}", customerId, tenantId);
            return null;
        }
        return customer;
    }

}
