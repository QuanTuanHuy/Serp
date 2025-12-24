/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateCustomerRequest;
import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateCustomerRequest;
import serp.project.crm.core.domain.dto.response.CustomerResponse;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.CustomerDtoMapper;
import serp.project.crm.core.service.ICustomerService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerUseCase {

    private final ICustomerService customerService;
    private final CustomerDtoMapper customerDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createCustomer(CreateCustomerRequest request, Long tenantId) {
        try {
            CustomerEntity customerEntity = customerDtoMapper.toEntity(request);
            CustomerEntity createdCustomer = customerService.createCustomer(customerEntity, tenantId);
            CustomerResponse response = customerDtoMapper.toResponse(createdCustomer);

            log.info("[CustomerUseCase] Customer created successfully with ID: {}", createdCustomer.getId());
            return responseUtils.success(response, "Customer created successfully");

        } catch (AppException e) {
            log.error("[CustomerUseCase] Error creating customer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[CustomerUseCase] Unexpected error creating customer: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateCustomer(Long id, UpdateCustomerRequest request, Long tenantId) {
        try {
            CustomerEntity updates = customerDtoMapper.toEntity(request);
            CustomerEntity updatedCustomer = customerService.updateCustomer(id, updates, tenantId);
            CustomerResponse response = customerDtoMapper.toResponse(updatedCustomer);

            log.info("[CustomerUseCase] Customer updated successfully: {}", id);
            return responseUtils.success(response, "Customer updated successfully");

        } catch (AppException e) {
            log.error("[CustomerUseCase] Error updating customer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[CustomerUseCase] Unexpected error updating customer: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getCustomerById(Long id, Long tenantId) {
        try {
            CustomerEntity customer = customerService.getCustomerById(id, tenantId)
                    .orElse(null);

            if (customer == null) {
                return responseUtils.notFound(ErrorMessage.CUSTOMER_NOT_FOUND);
            }

            CustomerResponse response = customerDtoMapper.toResponse(customer);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[CustomerUseCase] Error fetching customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch customer");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllCustomers(Long tenantId, PageRequest pageRequest) {
        try {
            var result = customerService.getAllCustomers(tenantId, pageRequest);

            List<CustomerResponse> customerResponses = result.getFirst().stream()
                    .map(customerDtoMapper::toResponse)
                    .toList();

            PageResponse<CustomerResponse> pageResponse = PageResponse.of(
                    customerResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[CustomerUseCase] Error fetching customers: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch customers");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> filterCustomers(CustomerFilterRequest request, Long tenantId) {
        try {

            PageRequest pageRequest = request.toPageRequest();
            var result = customerService.filterCustomers(request, tenantId, pageRequest);

            List<CustomerResponse> customerResponses = result.getFirst().stream()
                    .map(customerDtoMapper::toResponse)
                    .toList();
            PageResponse<CustomerResponse> pageResponse = PageResponse.of(
                    customerResponses, pageRequest, result.getSecond());
            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[CustomerUseCase] Error filtering customers: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to filter customers");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteCustomer(Long id, Long tenantId) {
        try {
            customerService.deleteCustomer(id, tenantId);

            log.info("[CustomerUseCase] Customer deleted successfully: {}", id);
            return responseUtils.status("Customer deleted successfully");

        } catch (AppException e) {
            log.error("[CustomerUseCase] Validation error deleting customer: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("[CustomerUseCase] Unexpected error deleting customer: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete customer");
        }
    }
}
