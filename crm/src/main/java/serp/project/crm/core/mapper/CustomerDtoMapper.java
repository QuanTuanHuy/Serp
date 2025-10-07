/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateCustomerRequest;
import serp.project.crm.core.domain.dto.request.UpdateCustomerRequest;
import serp.project.crm.core.domain.dto.response.AddressResponse;
import serp.project.crm.core.domain.dto.response.CustomerResponse;
import serp.project.crm.core.domain.entity.AddressEntity;
import serp.project.crm.core.domain.entity.CustomerEntity;

@Component
public class CustomerDtoMapper {

    public CustomerEntity toEntity(CreateCustomerRequest request) {
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

        return CustomerEntity.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(address)
                .taxId(request.getTaxId())
                .creditLimit(request.getCreditLimit())
                .activeStatus(request.getActiveStatus())
                .notes(request.getNotes())
                .build();
    }

    public CustomerEntity toEntity(UpdateCustomerRequest request) {
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

        return CustomerEntity.builder()
                .name(request.getName())
                .industry(request.getIndustry())
                .companySize(request.getCompanySize())
                .website(request.getWebsite())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(address)
                .taxId(request.getTaxId())
                .creditLimit(request.getCreditLimit())
                .activeStatus(request.getActiveStatus())
                .notes(request.getNotes())
                .build();
    }

    public CustomerResponse toResponse(CustomerEntity entity) {
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

        return CustomerResponse.builder()
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
