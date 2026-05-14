/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateAccountRequest;
import serp.project.crm.core.domain.dto.request.UpdateAccountRequest;
import serp.project.crm.core.domain.dto.response.AddressResponse;
import serp.project.crm.core.domain.dto.response.AccountResponse;
import serp.project.crm.core.domain.entity.AddressEntity;
import serp.project.crm.core.domain.entity.AccountEntity;

@Component
public class AccountDtoMapper {

    public AccountEntity toEntity(CreateAccountRequest request) {
        if (request == null) {
            return null;
        }

        AddressEntity address = AddressEntity.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();

        return AccountEntity.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(address)
                .taxId(request.getTaxId())
                .creditLimit(request.getCreditLimit())
                .tier(request.getTier())
                .preferredTimeSlots(request.getPreferredTimeSlots())
                .preferredDays(request.getPreferredDays())
                .language(request.getLanguage())
                .timezone(request.getTimezone())
                .notes(request.getNotes())
                .build();
    }

    public AccountEntity toEntity(UpdateAccountRequest request) {
        if (request == null) {
            return null;
        }

        AddressEntity address = null;
        if (request.getStreet() != null || request.getCity() != null ||
                request.getState() != null || request.getZipCode() != null ||
                request.getCountry() != null) {
            address = AddressEntity.builder()
                    .street(request.getStreet())
                    .city(request.getCity())
                    .state(request.getState())
                    .zipCode(request.getZipCode())
                    .country(request.getCountry())
                    .build();
        }

        return AccountEntity.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(address)
                .taxId(request.getTaxId())
                .tier(request.getTier())
                .preferredTimeSlots(request.getPreferredTimeSlots())
                .preferredDays(request.getPreferredDays())
                .language(request.getLanguage())
                .timezone(request.getTimezone())
                .notes(request.getNotes())
                .build();
    }

    public AccountResponse toResponse(AccountEntity entity) {
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

        return AccountResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .website(entity.getWebsite())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(addressResponse)
                .taxId(entity.getTaxId())
                .creditLimit(entity.getCreditLimit())
                .activeStatus(entity.getActiveStatus())
                .accountType(entity.getAccountType())
                .tier(entity.getTier())
                .preferredTimeSlots(entity.getPreferredTimeSlots())
                .preferredDays(entity.getPreferredDays())
                .language(entity.getLanguage())
                .timezone(entity.getTimezone())
                .totalRevenue(entity.getTotalRevenue())
                .totalOpportunities(entity.getTotalOpportunities())
                .wonOpportunities(entity.getWonOpportunities())
                .notes(entity.getNotes())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
