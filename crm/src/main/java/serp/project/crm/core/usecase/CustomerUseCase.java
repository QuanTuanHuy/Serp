/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateCustomerRequest;
import serp.project.crm.core.domain.dto.request.UpdateCustomerRequest;
import serp.project.crm.core.domain.dto.response.CustomerResponse;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.mapper.CustomerDtoMapper;
import serp.project.crm.core.service.ICustomerService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

/**
 * Customer Use Case - Orchestrates customer management workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerUseCase {

    private final ICustomerService customerService;
    private final CustomerDtoMapper customerDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createCustomer(CreateCustomerRequest request, Long tenantId) {
        try {
            log.info("Creating customer: {}, tenant: {}", request.getName(), tenantId);

            CustomerEntity customerEntity = customerDtoMapper.toEntity(request);
            CustomerEntity createdCustomer = customerService.createCustomer(customerEntity, tenantId);
            CustomerResponse response = customerDtoMapper.toResponse(createdCustomer);

            log.info("Customer created successfully with ID: {}", createdCustomer.getId());
            return responseUtils.success(response, "Customer created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating customer: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create customer");
        }
    }

    @Transactional
    public GeneralResponse<?> updateCustomer(Long id, UpdateCustomerRequest request, Long tenantId) {
        try {
            log.info("Updating customer ID: {}, tenant: {}", id, tenantId);

            CustomerEntity updates = customerDtoMapper.toEntity(request);
            CustomerEntity updatedCustomer = customerService.updateCustomer(id, updates, tenantId);
            CustomerResponse response = customerDtoMapper.toResponse(updatedCustomer);

            log.info("Customer updated successfully: {}", id);
            return responseUtils.success(response, "Customer updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating customer: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update customer");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getCustomerById(Long id, Long tenantId) {
        try {
            log.info("Fetching customer ID: {}, tenant: {}", id, tenantId);

            CustomerEntity customer = customerService.getCustomerById(id, tenantId)
                    .orElse(null);

            if (customer == null) {
                return responseUtils.notFound("Customer not found");
            }

            CustomerResponse response = customerDtoMapper.toResponse(customer);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch customer");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllCustomers(Long tenantId, PageRequest pageRequest) {
        try {
            log.info("Fetching all customers for tenant: {}", tenantId);

            var result = customerService.getAllCustomers(tenantId, pageRequest);

            List<CustomerResponse> customerResponses = result.getFirst().stream()
                    .map(customerDtoMapper::toResponse)
                    .toList();

            PageResponse<CustomerResponse> pageResponse = PageResponse.of(
                    customerResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching customers: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch customers");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteCustomer(Long id, Long tenantId) {
        try {
            log.info("Deleting customer ID: {}, tenant: {}", id, tenantId);

            customerService.deleteCustomer(id, tenantId);

            log.info("Customer deleted successfully: {}", id);
            return responseUtils.status("Customer deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting customer: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete customer");
        }
    }
}
