/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.port.store.ICustomerPort;
import serp.project.crm.infrastructure.store.mapper.CustomerMapper;
import serp.project.crm.infrastructure.store.model.CustomerModel;
import serp.project.crm.infrastructure.store.repository.CustomerRepository;
import serp.project.crm.infrastructure.store.specification.CustomerSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerAdapter implements ICustomerPort {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerEntity save(CustomerEntity customerEntity) {
        var model = customerMapper.toModel(customerEntity);
        var savedModel = customerRepository.save(model);
        return customerMapper.toEntity(savedModel);
    }

    @Override
    public Optional<CustomerEntity> findById(Long id, Long tenantId) {
        return customerRepository.findByIdAndTenantId(id, tenantId)
                .map(customerMapper::toEntity);
    }

    @Override
    public Optional<CustomerEntity> findByEmail(String email, Long tenantId) {
        return customerRepository.findByEmailAndTenantId(email, tenantId)
                .map(customerMapper::toEntity);
    }

    @Override
    public Pair<List<CustomerEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var page = customerRepository.findByTenantId(tenantId, customerMapper.toPageable(pageRequest));
        return customerMapper.pageToPair(page.map(customerMapper::toEntity));
    }

    @Override
    public Pair<List<CustomerEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest) {
        var page = customerRepository.searchByKeyword(tenantId, keyword, customerMapper.toPageable(pageRequest));
        return customerMapper.pageToPair(page.map(customerMapper::toEntity));
    }

    @Override
    public List<CustomerEntity> findByParentCustomerId(Long parentCustomerId, Long tenantId) {
        return customerRepository.findByParentCustomerIdAndTenantId(parentCustomerId, tenantId)
                .stream()
                .map(customerMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<CustomerEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest) {
        var page = customerRepository.findByTenantIdAndActiveStatus(tenantId, activeStatus.name(),
                customerMapper.toPageable(pageRequest));
        return customerMapper.pageToPair(page.map(customerMapper::toEntity));
    }

    @Override
    public Long countByActiveStatus(ActiveStatus activeStatus, Long tenantId) {
        return customerRepository.countByTenantIdAndActiveStatus(tenantId, activeStatus.name());
    }

    @Override
    public Boolean existsByEmail(String email, Long tenantId) {
        return customerRepository.existsByEmailAndTenantId(email, tenantId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        customerRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(customerRepository::delete);
    }

    @Override
    public List<CustomerEntity> findTopByRevenue(Long tenantId, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "totalRevenue"));
        return customerRepository.findByTenantId(tenantId, pageable)
                .stream()
                .map(customerMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<CustomerEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        var page = customerRepository
                .findByTenantIdAndIndustry(tenantId, industry, customerMapper.toPageable(pageRequest))
                .map(customerMapper::toEntity);
        return customerMapper.pageToPair(page);
    }

    @Override
    public Pair<List<CustomerEntity>, Long> filter(CustomerFilterRequest filter, PageRequest pageRequest, Long tenantId) {
        var pageable = customerMapper.toPageable(pageRequest);
        Specification<CustomerModel> spec = CustomerSpecification.build(filter, tenantId);
        var page = customerRepository.findAll(spec, pageable)
                .map(customerMapper::toEntity);
        return customerMapper.pageToPair(page);
    }
}
