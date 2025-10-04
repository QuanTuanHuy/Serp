/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.mapper;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.request.CreateContactRequest;
import serp.project.crm.core.domain.dto.request.UpdateContactRequest;
import serp.project.crm.core.domain.dto.response.AddressResponse;
import serp.project.crm.core.domain.dto.response.ContactResponse;
import serp.project.crm.core.domain.entity.AddressEntity;
import serp.project.crm.core.domain.entity.ContactEntity;

@Component
public class ContactDtoMapper {

    public ContactEntity toEntity(CreateContactRequest request) {
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

        return ContactEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobPosition(request.getJobPosition())
                .customerId(request.getCustomerId())
                .isPrimary(request.getIsPrimary())
                .address(address)
                .contactType(request.getContactType())
                .activeStatus(request.getActiveStatus())
                .linkedInUrl(request.getLinkedInUrl())
                .twitterHandle(request.getTwitterHandle())
                .notes(request.getNotes())
                .build();
    }

    public ContactEntity toEntity(UpdateContactRequest request) {
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

        return ContactEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .jobPosition(request.getJobPosition())
                .address(address)
                .contactType(request.getContactType())
                .activeStatus(request.getActiveStatus())
                .linkedInUrl(request.getLinkedInUrl())
                .twitterHandle(request.getTwitterHandle())
                .notes(request.getNotes())
                .build();
    }

    public ContactResponse toResponse(ContactEntity entity) {
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

        return ContactResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .jobPosition(entity.getJobPosition())
                .customerId(entity.getCustomerId())
                .isPrimary(entity.getIsPrimary())
                .address(addressResponse)
                .contactType(entity.getContactType())
                .activeStatus(entity.getActiveStatus())
                .linkedInUrl(entity.getLinkedInUrl())
                .twitterHandle(entity.getTwitterHandle())
                .notes(entity.getNotes())
                .tenantId(entity.getTenantId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
