/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.ConvertLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.domain.dto.response.AddressResponse;
import serp.project.crm.core.domain.dto.response.LeadConversionResponse;
import serp.project.crm.core.domain.dto.response.LeadResponse;
import serp.project.crm.core.domain.entity.AddressEntity;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.entity.CustomerEntity;
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
                .leadSource(request.getLeadSource())
                .assignedTo(request.getAssignedTo())
                .estimatedValue(request.getEstimatedValue())
                .expectedCloseDate(request.getExpectedCloseDate())
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
                .leadSource(request.getLeadSource())
                .leadStatus(request.getLeadStatus())
                .assignedTo(request.getAssignedTo())
                .estimatedValue(request.getEstimatedValue())
                .probability(request.getProbability())
                .expectedCloseDate(request.getExpectedCloseDate())
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
                .leadSource(entity.getLeadSource())
                .leadStatus(entity.getLeadStatus())
                .assignedTo(entity.getAssignedTo())
                .estimatedValue(entity.getEstimatedValue())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .notes(entity.getNotes())
                .convertedOpportunityId(entity.getConvertedOpportunityId())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    // ========== Conversion Mappers ==========

    public CustomerEntity toCustomerEntity(LeadEntity lead) {
        if (lead == null) {
            return null;
        }

        return CustomerEntity.builder()
                .name(lead.getCompany())
                .industry(lead.getIndustry())
                .companySize(lead.getCompanySize())
                .website(lead.getWebsite())
                .phone(lead.getPhone())
                .email(lead.getEmail())
                .address(lead.getAddress())
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    public ContactEntity toContactEntity(LeadEntity lead, Long customerId) {
        if (lead == null) {
            return null;
        }

        return ContactEntity.builder()
                .customerId(customerId)
                .name(lead.getName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .jobPosition(lead.getJobTitle())
                .contactType(ContactType.INDIVIDUAL)
                .isPrimary(true)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    public OpportunityEntity toOpportunityEntity(LeadEntity lead, Long customerId, ConvertLeadRequest request) {
        if (lead == null) {
            return null;
        }

        return OpportunityEntity.builder()
                .name(request.getOpportunityName() != null ? request.getOpportunityName()
                        : lead.getCompany() + " - " + lead.getName())
                .description(request.getOpportunityDescription())
                .leadId(lead.getId())
                .customerId(customerId)
                .stage(OpportunityStage.PROSPECTING)
                .estimatedValue(request.getOpportunityAmount() != null ? request.getOpportunityAmount()
                        : lead.getEstimatedValue())
                .expectedCloseDate(lead.getExpectedCloseDate())
                .build();
    }

    public LeadConversionResponse toConversionResponse(Long leadId, Long customerId, Long opportunityId,
            Long contactId) {
        return LeadConversionResponse.builder()
                .leadId(leadId)
                .customerId(customerId)
                .opportunityId(opportunityId)
                .contactId(contactId)
                .message("Lead converted successfully")
                .build();
    }
}
