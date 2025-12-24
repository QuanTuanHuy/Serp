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
import serp.project.crm.core.domain.dto.request.ConvertLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.dto.request.QualifyLeadRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.domain.dto.response.LeadConversionResponse;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.LeadDtoMapper;
import serp.project.crm.core.service.*;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadUseCase {

    private final ILeadService leadService;
    private final ICustomerService customerService;
    private final IOpportunityService opportunityService;
    private final IContactService contactService;

    private final LeadDtoMapper leadDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createLead(CreateLeadRequest request, Long tenantId) {
        try {
            LeadEntity leadEntity = leadDtoMapper.toEntity(request);
            LeadEntity createdLead = leadService.createLead(leadEntity, tenantId);
            LeadResponse response = leadDtoMapper.toResponse(createdLead);

            return responseUtils.success(response, "Lead created successfully");

        } catch (AppException e) {
            log.error("Error creating lead: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating lead: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateLead(Long id, Long userId, UpdateLeadRequest request, Long tenantId) {
        try {
            LeadEntity updates = leadDtoMapper.toEntity(request);
            updates.setUpdatedBy(userId);
            LeadEntity updatedLead = leadService.updateLead(id, updates, tenantId);
            LeadResponse response = leadDtoMapper.toResponse(updatedLead);

            return responseUtils.success(response, "Lead updated successfully");

        } catch (AppException e) {
            log.error("Error updating lead: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating lead: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> qualifyLead(QualifyLeadRequest request, Long tenantId) {
        try {
            LeadEntity qualifiedLead = leadService.qualifyLead(request.getLeadId(),
                    tenantId);

            LeadResponse response = leadDtoMapper.toResponse(qualifiedLead);

            log.info("Lead qualified successfully: {}", request.getLeadId());
            return responseUtils.success(response, "Lead qualified successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error qualifying lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("State error qualifying lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error qualifying lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to qualify lead");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> convertLead(ConvertLeadRequest request, Long tenantId) {
        try {
            LeadEntity lead = leadService.getLeadById(request.getLeadId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.LEAD_NOT_FOUND));
            if (!lead.canBeConverted()) {
                return responseUtils.badRequest(ErrorMessage.LEAD_CANNOT_BE_CONVERTED);
            }

            Long customerId;
            if (Boolean.TRUE.equals(request.getCreateNewCustomer())) {
                CustomerEntity customer = leadDtoMapper.toCustomerEntity(lead);

                CustomerEntity createdCustomer = customerService.createCustomer(customer, tenantId);
                customerId = createdCustomer.getId();
                log.info("Created new customer ID: {} from lead", customerId);
            } else {
                customerId = request.getExistingCustomerId();
                if (customerId == null) {
                    return responseUtils
                            .badRequest("Either createNewCustomer must be true or existingCustomerId must be provided");
                }
                customerService.getCustomerById(customerId, tenantId)
                        .orElseThrow(() -> new AppException(ErrorMessage.CUSTOMER_NOT_FOUND));
                log.info("Using existing customer ID: {}", customerId);
            }

            ContactEntity contact = leadDtoMapper.toContactEntity(lead, customerId);
            ContactEntity createdContact = contactService.createContact(contact, tenantId, tenantId); // TODO: fix userId
            log.info("Created contact ID: {} from lead", createdContact.getId());

            OpportunityEntity opportunity = leadDtoMapper.toOpportunityEntity(lead, customerId, request);
            OpportunityEntity createdOpportunity = opportunityService.createOpportunity(opportunity, tenantId);

            leadService.convertLead(request.getLeadId(), customerId, createdOpportunity.getId(), tenantId);

            LeadConversionResponse response = leadDtoMapper.toConversionResponse(
                    request.getLeadId(), customerId, createdOpportunity.getId(), createdContact.getId());

            log.info("Lead conversion completed successfully");
            return responseUtils.success(response, "Lead converted successfully");

        } catch (AppException e) {
            log.error("Error converting lead: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error converting lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to convert lead");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getLeadById(Long id, Long tenantId) {
        try {
            LeadEntity lead = leadService.getLeadById(id, tenantId)
                    .orElse(null);
            if (lead == null) {
                return responseUtils.notFound(ErrorMessage.LEAD_NOT_FOUND);
            }

            LeadResponse response = leadDtoMapper.toResponse(lead);
            return responseUtils.success(response, "Lead retrieved successfully");

        } catch (Exception e) {
            log.error("Error fetching lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch lead");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllLeads(Long tenantId, PageRequest pageRequest) {
        try {
            var result = leadService.getAllLeads(tenantId, pageRequest);

            List<LeadResponse> leadResponses = result.getFirst().stream()
                    .map(leadDtoMapper::toResponse)
                    .toList();

            PageResponse<LeadResponse> pageResponse = PageResponse.of(
                    leadResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching leads: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch leads");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> filterLeads(LeadFilterRequest request, Long tenantId) {
        try {
            LeadFilterRequest safeRequest = request != null ? request : LeadFilterRequest.builder().build();
            PageRequest pageRequest = safeRequest.toPageRequest();
            var result = leadService.filterLeads(safeRequest, tenantId, pageRequest);

            List<LeadResponse> leadResponses = result.getFirst().stream()
                    .map(leadDtoMapper::toResponse)
                    .toList();

            PageResponse<LeadResponse> pageResponse = PageResponse.of(
                    leadResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error filtering leads: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to filter leads");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteLead(Long id, Long tenantId) {
        try {
            leadService.deleteLead(id, tenantId);

            log.info("Lead deleted successfully: {}", id);
            return responseUtils.status("Lead deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete lead");
        }
    }

}
