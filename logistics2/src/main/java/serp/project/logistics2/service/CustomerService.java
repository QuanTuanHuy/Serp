package serp.project.logistics2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.logistics2.entity.CustomerEntity;
import serp.project.logistics2.repository.CustomerRepository;
import serp.project.logistics2.repository.specification.CustomerSpecification;
import serp.project.logistics2.util.PaginationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

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
