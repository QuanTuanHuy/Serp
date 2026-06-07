/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadStatusRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.domain.dto.response.AddressResponse;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.dto.response.LeadStatusTransitionResponse;
import serp.project.crm.core.domain.entity.AddressEntity;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.core.domain.enums.OpportunityStage;

@Component
public class LeadDtoMapper {

    public LeadEntity toEntity(CreateLeadRequest request) {
        if (request == null) {
            return null;
        }

        AddressEntity address = AddressEntity.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getPostalCode())
                .country(request.getCountry())
                .build();

        return LeadEntity.builder()
                .company(request.getCompany())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobTitle(request.getJobTitle())
                .address(address)
                .territoryCode(request.getTerritoryCode())
                .leadSource(request.getLeadSource())
                .assignedTo(request.getAssignedTo())
                .estimatedValue(request.getEstimatedValue())
                .leadScore(request.getLeadScore())
                .followUpDate(request.getFollowUpDate())
                .notes(request.getNotes())
                .build();
    }

    public LeadEntity toEntity(UpdateLeadRequest request) {
        if (request == null) {
            return null;
        }

        AddressEntity address = null;
        if (request.getStreet() != null || request.getCity() != null ||
                request.getState() != null || request.getPostalCode() != null ||
                request.getCountry() != null) {
            address = AddressEntity.builder()
                    .street(request.getStreet())
                    .city(request.getCity())
                    .state(request.getState())
                    .zipCode(request.getPostalCode())
                    .country(request.getCountry())
                    .build();
        }

        return LeadEntity.builder()
                .company(request.getCompany())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobTitle(request.getJobTitle())
                .address(address)
                .territoryCode(request.getTerritoryCode())
                .leadSource(request.getLeadSource())
                .leadStatus(request.getLeadStatus())
                .assignedTo(request.getAssignedTo())
                .estimatedValue(request.getEstimatedValue())
                .leadScore(request.getLeadScore())
                .followUpDate(request.getFollowUpDate())
                .notes(request.getNotes())
                .build();
    }

    public LeadResponse toResponse(LeadEntity entity) {
        if (entity == null) {
            return null;
        }

        AddressResponse addressResponse = null;
        if (entity.getAddress() != null) {
            addressResponse = AddressResponse.builder()
                    .street(entity.getAddress().getStreet())
                    .city(entity.getAddress().getCity())
                    .state(entity.getAddress().getState())
                    .postalCode(entity.getAddress().getZipCode())
                    .country(entity.getAddress().getCountry())
                    .build();
        }

        return LeadResponse.builder()
                .id(entity.getId())
                .company(entity.getCompany())
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .website(entity.getWebsite())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .jobTitle(entity.getJobTitle())
                .address(addressResponse)
                .territoryCode(entity.getTerritoryCode())
                .leadSource(entity.getLeadSource())
                .leadStatus(entity.getLeadStatus())
                .assignedTo(entity.getAssignedTo())
                .estimatedValue(entity.getEstimatedValue())
                .leadScore(entity.getLeadScore())
                .followUpDate(entity.getFollowUpDate())
                .notes(entity.getNotes())
                .convertedOpportunityId(entity.getConvertedOpportunityId())
                .convertedAccountId(entity.getConvertedAccountId())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    // ========== Conversion Mappers ==========

    public AccountEntity toAccountEntity(LeadEntity lead) {
        return toAccountEntity(lead, null);
    }

    public AccountEntity toAccountEntity(LeadEntity lead, UpdateLeadStatusRequest.AccountData accountData) {
        if (lead == null) {
            return null;
        }

        return AccountEntity.builder()
                .name(accountData != null && accountData.getName() != null ? accountData.getName() : lead.getCompany())
                .industry(lead.getIndustry())
                .companySize(lead.getCompanySize())
                .website(lead.getWebsite())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .address(lead.getAddress())
                .creditLimit(accountData != null ? accountData.getCreditLimit() : null)
                .notes(accountData != null ? accountData.getNotes() : null)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    public ContactEntity toContactEntity(LeadEntity lead, Long accountId) {
        if (lead == null) {
            return null;
        }

        return ContactEntity.builder()
                .accountId(accountId)
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .jobPosition(lead.getJobTitle())
                .contactType(ContactType.SECONDARY)
                .isPrimary(true)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    public OpportunityEntity toOpportunityEntity(LeadEntity lead, Long accountId,
            UpdateLeadStatusRequest.ConversionData conversionData) {
        if (lead == null) {
            return null;
        }

        UpdateLeadStatusRequest.OpportunityData opportunityData = conversionData != null
                ? conversionData.getOpportunityData()
                : null;

        return OpportunityEntity.builder()
                .name(opportunityData != null && opportunityData.getName() != null ? opportunityData.getName()
                        : lead.getCompany() + " - " + lead.getName())
                .description(opportunityData != null ? opportunityData.getNotes() : null)
                .leadId(lead.getId())
                .accountId(accountId)
                .stage(OpportunityStage.PROSPECTING)
                .estimatedValue(opportunityData != null && opportunityData.getAmount() != null ? opportunityData.getAmount()
                        : lead.getEstimatedValue())
                .expectedCloseDate(opportunityData != null && opportunityData.getExpectedCloseDate() != null
                        ? opportunityData.getExpectedCloseDate()
                        : lead.getFollowUpDate())
                .build();
    }

    public LeadStatusTransitionResponse toStatusTransitionResponse(LeadEntity lead, LeadStatusTransitionResponse base) {
        return LeadStatusTransitionResponse.builder()
                .lead(toResponse(lead))
                .fromStatus(base.getFromStatus())
                .toStatus(base.getToStatus())
                .accountId(base.getAccountId())
                .opportunityId(base.getOpportunityId())
                .contactId(base.getContactId())
                .message(base.getMessage())
                .build();
    }
}
