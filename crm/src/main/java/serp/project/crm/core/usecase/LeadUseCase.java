/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.ConvertLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.QualifyLeadRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.domain.dto.response.LeadConversionResponse;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.entity.*;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.mapper.LeadDtoMapper;
import serp.project.crm.core.service.*;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

/**
 * Lead Use Case - Orchestrates lead management workflows
 * Coordinates between multiple services for complex business operations
 */
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

    @Transactional
    public GeneralResponse<?> createLead(CreateLeadRequest request, Long tenantId) {
        try {
            log.info("Creating lead for company: {}, tenant: {}", request.getCompany(), tenantId);

            // Map DTO to Entity
            LeadEntity leadEntity = leadDtoMapper.toEntity(request);

            // Create lead using service
            LeadEntity createdLead = leadService.createLead(leadEntity, tenantId);

            // Map Entity to Response DTO
            LeadResponse response = leadDtoMapper.toResponse(createdLead);

            log.info("Lead created successfully with ID: {}", createdLead.getId());
            return responseUtils.success(response, "Lead created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create lead");
        }
    }

    @Transactional
    public GeneralResponse<?> updateLead(Long id, UpdateLeadRequest request, Long tenantId) {
        try {
            log.info("Updating lead ID: {}, tenant: {}", id, tenantId);

            // Map DTO to Entity
            LeadEntity updates = leadDtoMapper.toEntity(request);

            // Update lead using service
            LeadEntity updatedLead = leadService.updateLead(id, updates, tenantId);

            // Map Entity to Response DTO
            LeadResponse response = leadDtoMapper.toResponse(updatedLead);

            log.info("Lead updated successfully: {}", id);
            return responseUtils.success(response, "Lead updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update lead");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getLeadById(Long id, Long tenantId) {
        try {
            log.info("Fetching lead ID: {}, tenant: {}", id, tenantId);

            LeadEntity lead = leadService.getLeadById(id, tenantId)
                    .orElse(null);

            if (lead == null) {
                return responseUtils.notFound("Lead not found");
            }

            LeadResponse response = leadDtoMapper.toResponse(lead);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch lead");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllLeads(Long tenantId, PageRequest pageRequest) {
        try {
            log.info("Fetching all leads for tenant: {}", tenantId);

            Pair<List<LeadEntity>, Long> result = leadService.getAllLeads(tenantId, pageRequest);

            List<LeadResponse> leadResponses = result.getFirst().stream()
                    .map(leadDtoMapper::toResponse)
                    .toList();

            return responseUtils.success(Pair.of(leadResponses, result.getSecond()));

        } catch (Exception e) {
            log.error("Error fetching leads: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch leads");
        }
    }

    @Transactional
    public GeneralResponse<?> qualifyLead(QualifyLeadRequest request, Long tenantId) {
        try {
            log.info("Qualifying lead ID: {}, tenant: {}", request.getLeadId(), tenantId);

            // Qualify the lead
            LeadEntity qualifiedLead = leadService.qualifyLead(request.getLeadId(), tenantId);

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

    /**
     * Convert lead to customer, opportunity, and contact
     * This is a complex workflow involving multiple services
     */
    @Transactional
    public GeneralResponse<?> convertLead(ConvertLeadRequest request, Long tenantId) {
        try {
            log.info("Converting lead ID: {}, tenant: {}", request.getLeadId(), tenantId);

            // 1. Fetch the lead
            LeadEntity lead = leadService.getLeadById(request.getLeadId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Lead not found"));

            // 2. Create or use existing customer
            Long customerId;
            if (Boolean.TRUE.equals(request.getCreateNewCustomer())) {
                // Create new customer from lead data
                CustomerEntity customer = CustomerEntity.builder()
                        .name(lead.getCompany())
                        .industry(lead.getIndustry())
                        .companySize(lead.getCompanySize())
                        .website(lead.getWebsite())
                        .phone(lead.getPhone())
                        .email(lead.getEmail())
                        .address(lead.getAddress())
                        .activeStatus(ActiveStatus.ACTIVE)
                        .build();
                
                CustomerEntity createdCustomer = customerService.createCustomer(customer, tenantId);
                customerId = createdCustomer.getId();
                log.info("Created new customer ID: {} from lead", customerId);
            } else {
                customerId = request.getExistingCustomerId();
                if (customerId == null) {
                    return responseUtils.badRequest("Either createNewCustomer must be true or existingCustomerId must be provided");
                }
                log.info("Using existing customer ID: {}", customerId);
            }

            // 3. Create contact from lead
            ContactEntity contact = ContactEntity.builder()
                    .customerId(customerId)
                    .name(lead.getName())
                    .email(lead.getEmail())
                    .phone(lead.getPhone())
                    .jobPosition(lead.getJobTitle())
                    .contactType(ContactType.INDIVIDUAL)
                    .isPrimary(true)
                    .activeStatus(ActiveStatus.ACTIVE)
                    .build();
            
            ContactEntity createdContact = contactService.createContact(contact, tenantId);
            log.info("Created contact ID: {} from lead", createdContact.getId());

            // 4. Create opportunity from lead
            OpportunityEntity opportunity = OpportunityEntity.builder()
                    .name(request.getOpportunityName() != null ? 
                          request.getOpportunityName() : 
                          lead.getCompany() + " - " + lead.getName())
                    .customerId(customerId)
                    .estimatedValue(request.getOpportunityAmount() != null ? 
                            request.getOpportunityAmount() : 
                            lead.getEstimatedValue())
                    .description(request.getOpportunityDescription())
                    .stage(OpportunityStage.PROSPECTING)
                    .expectedCloseDate(lead.getExpectedCloseDate())
                    .build();
            
            OpportunityEntity createdOpportunity = opportunityService.createOpportunity(opportunity, tenantId);
            log.info("Created opportunity ID: {} from lead", createdOpportunity.getId());

            // 5. Convert the lead (marks it as CONVERTED)
            leadService.convertLead(request.getLeadId(), tenantId);

            // 6. Build response
            LeadConversionResponse response = LeadConversionResponse.builder()
                    .leadId(request.getLeadId())
                    .customerId(customerId)
                    .opportunityId(createdOpportunity.getId())
                    .contactId(createdContact.getId())
                    .message("Lead converted successfully")
                    .build();

            log.info("Lead conversion completed successfully");
            return responseUtils.success(response, "Lead converted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error converting lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("State error converting lead: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error converting lead: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to convert lead");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteLead(Long id, Long tenantId) {
        try {
            log.info("Deleting lead ID: {}, tenant: {}", id, tenantId);

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
