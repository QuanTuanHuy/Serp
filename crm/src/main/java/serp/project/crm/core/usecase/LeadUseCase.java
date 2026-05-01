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
import serp.project.crm.core.domain.dto.request.AssignLeadRequest;
import serp.project.crm.core.domain.dto.request.BulkAssignLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadStatusRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.domain.dto.response.LeadAssignResponse;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.dto.response.LeadStatusTransitionResponse;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.LeadStatus;
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
    private final IAccountService accountService;
    private final IOpportunityService opportunityService;
    private final IContactService contactService;
    private final ITerritoryService territoryService;

    private final LeadDtoMapper leadDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createLead(CreateLeadRequest request, Long tenantId) {
        try {
            LeadEntity leadEntity = leadDtoMapper.toEntity(request);
            territoryService.resolveTerritory(request.getTerritoryCode(), request.getState(), request.getCity(), tenantId)
                    .ifPresent(territory -> leadEntity.setTerritoryCode(territory.getTerritoryCode()));
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
            territoryService.resolveTerritory(request.getTerritoryCode(), request.getState(), request.getCity(), tenantId)
                    .ifPresent(territory -> updates.setTerritoryCode(territory.getTerritoryCode()));
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
    public GeneralResponse<?> updateLeadStatus(Long id, Long userId, UpdateLeadStatusRequest request, Long tenantId) {
        try {
            LeadEntity lead = leadService.getLeadById(id, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.LEAD_NOT_FOUND));
            LeadStatus currentStatus = lead.getLeadStatus() != null ? lead.getLeadStatus() : LeadStatus.NEW;
            if (request.getFromStatus() != null && request.getFromStatus() != currentStatus) {
                throw new AppException("Lead status changed by another action. Please refresh and try again.",
                        serp.project.crm.core.domain.constant.Constants.HttpStatusCode.CONFLICT);
            }

            LeadStatusTransitionResponse transitionResponse = switch (request.getToStatus()) {
                case QUALIFIED -> qualifyLead(id, request, userId, tenantId, currentStatus);
                case DISQUALIFIED -> disqualifyLead(id, request, userId, tenantId, currentStatus);
                case CONVERTED -> convertLead(id, request, userId, tenantId, currentStatus);
                default -> updateWorkingLeadStatus(id, request, userId, tenantId, currentStatus);
            };

            return responseUtils.success(transitionResponse, transitionResponse.getMessage());

        } catch (AppException e) {
            log.error("Error updating lead status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating lead status: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> assignLead(Long id, AssignLeadRequest request, Long userId, Long tenantId) {
        LeadEntity assignedLead = leadService.assignLead(id, request.getAssignedTo(), userId, tenantId);
        LeadResponse response = leadDtoMapper.toResponse(assignedLead);
        return responseUtils.success(response, "Lead assigned successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> bulkAssignLeads(BulkAssignLeadRequest request, Long userId, Long tenantId) {
        int assignedCount = 0;
        for (Long leadId : request.getLeadIds()) {
            leadService.assignLead(leadId, request.getAssignedTo(), userId, tenantId);
            assignedCount++;
        }
        LeadAssignResponse response = LeadAssignResponse.builder()
                .assignedCount(assignedCount)
                .assignedTo(request.getAssignedTo())
                .build();
        return responseUtils.success(response, "Lead(s) assigned successfully");
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

        } catch (AppException e) {
            log.error("Error deleting lead: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting lead: {}", e.getMessage(), e);
            throw e;
        }
    }

    private LeadStatusTransitionResponse updateWorkingLeadStatus(Long id, UpdateLeadStatusRequest request, Long userId,
            Long tenantId, LeadStatus currentStatus) {
        LeadEntity updatedLead = leadService.updateLeadStatus(
                id,
                request.getFromStatus(),
                request.getToStatus(),
                request.getNotes(),
                userId,
                tenantId);

        return leadDtoMapper.toStatusTransitionResponse(
                updatedLead,
                LeadStatusTransitionResponse.builder()
                        .fromStatus(currentStatus)
                        .toStatus(request.getToStatus())
                        .message("Lead status updated successfully")
                        .build());
    }

    private LeadStatusTransitionResponse qualifyLead(Long id, UpdateLeadStatusRequest request, Long userId,
            Long tenantId, LeadStatus currentStatus) {
        if (request.getNotes() == null || request.getNotes().isBlank()) {
            throw new AppException("Qualification notes are required");
        }

        LeadEntity qualifiedLead = leadService.qualifyLead(id, request.getNotes(), userId, tenantId);
        log.info("Lead qualified successfully: {}", id);

        return leadDtoMapper.toStatusTransitionResponse(
                qualifiedLead,
                LeadStatusTransitionResponse.builder()
                        .fromStatus(currentStatus)
                        .toStatus(LeadStatus.QUALIFIED)
                        .message("Lead qualified successfully")
                        .build());
    }

    private LeadStatusTransitionResponse disqualifyLead(Long id, UpdateLeadStatusRequest request, Long userId,
            Long tenantId, LeadStatus currentStatus) {
        if (request.getNotes() == null || request.getNotes().isBlank()) {
            throw new AppException("Disqualification notes are required");
        }

        LeadEntity disqualifiedLead = leadService.disqualifyLead(id, request.getNotes(), userId, tenantId);

        return leadDtoMapper.toStatusTransitionResponse(
                disqualifiedLead,
                LeadStatusTransitionResponse.builder()
                        .fromStatus(currentStatus)
                        .toStatus(LeadStatus.DISQUALIFIED)
                        .message("Lead disqualified successfully")
                        .build());
    }

    private LeadStatusTransitionResponse convertLead(Long id, UpdateLeadStatusRequest request, Long userId,
            Long tenantId, LeadStatus currentStatus) {
        LeadEntity lead = leadService.getLeadById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.LEAD_NOT_FOUND));
        if (lead.getLeadStatus() != LeadStatus.QUALIFIED) {
            throw new AppException(ErrorMessage.LEAD_CANNOT_BE_CONVERTED);
        }

        UpdateLeadStatusRequest.ConversionData conversion = request.getConversion() != null
                ? request.getConversion()
                : UpdateLeadStatusRequest.ConversionData.builder().build();

        boolean createOpportunity = !Boolean.FALSE.equals(conversion.getCreateOpportunity());
        boolean createAccount = !Boolean.FALSE.equals(conversion.getCreateAccount());
        if (!createOpportunity && !createAccount && conversion.getExistingAccountId() == null) {
            throw new AppException("Conversion must create or link an account, or create an opportunity");
        }

        if (createOpportunity && !createAccount && conversion.getExistingAccountId() == null) {
            throw new AppException("Account is required when creating opportunity from lead");
        }

        if (createOpportunity) {
            var opportunityData = conversion.getOpportunityData();
            boolean hasOpportunityAmount = opportunityData != null && opportunityData.getAmount() != null
                    && opportunityData.getAmount().signum() > 0;
            boolean hasLeadEstimatedValue = lead.getEstimatedValue() != null && lead.getEstimatedValue().signum() > 0;
            if (!hasOpportunityAmount && !hasLeadEstimatedValue) {
                throw new AppException(
                        "Lead must have estimated value > 0 to create an opportunity. To create account only, estimated value is not required");
            }
        }

        Long accountId;
        if (createAccount) {
            AccountEntity account = leadDtoMapper.toAccountEntity(lead, conversion.getAccountData());
            if (account.getName() == null || account.getName().isBlank()) {
                throw new AppException("Account name is required when creating account from lead");
            }

            AccountEntity createdAccount = accountService.createAccount(account, tenantId);
            accountId = createdAccount.getId();
            log.info("Created new Account ID: {} from lead", accountId);
        } else if (conversion.getExistingAccountId() != null) {
            accountId = conversion.getExistingAccountId();
            accountService.getAccountById(accountId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
            log.info("Using existing Account ID: {}", accountId);
        } else {
            accountId = null;
        }

        ContactEntity createdContact = null;
        if (accountId != null) {
            ContactEntity contact = leadDtoMapper.toContactEntity(lead, accountId);
            createdContact = contactService.createContact(contact, userId, tenantId);
            log.info("Created contact ID: {} from lead", createdContact.getId());
        }

        OpportunityEntity createdOpportunity = null;
        if (createOpportunity) {
            OpportunityEntity opportunity = leadDtoMapper.toOpportunityEntity(lead, accountId, conversion);
            createdOpportunity = opportunityService.createOpportunity(opportunity, tenantId);
        }

        LeadEntity convertedLead = leadService.convertLead(
                id,
                accountId,
                createdOpportunity != null ? createdOpportunity.getId() : null,
                userId,
                tenantId);

        log.info("Lead conversion completed successfully");
        return leadDtoMapper.toStatusTransitionResponse(
                convertedLead,
                LeadStatusTransitionResponse.builder()
                        .fromStatus(currentStatus)
                        .toStatus(LeadStatus.CONVERTED)
                        .accountId(accountId)
                        .opportunityId(createdOpportunity != null ? createdOpportunity.getId() : null)
                        .contactId(createdContact != null ? createdContact.getId() : null)
                        .message("Lead converted successfully")
                        .build());
    }
}
