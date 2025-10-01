/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;
import serp.project.crm.infrastructure.store.model.ContactModel;

@Component
@RequiredArgsConstructor
public class ContactMapper extends BaseMapper {

    public ContactEntity toEntity(ContactModel model) {
        if (model == null) {
            return null;
        }

        return ContactEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .customerId(model.getCustomerId())
                .name(model.getName())
                .email(model.getEmail())
                .phone(model.getPhone())
                .jobPosition(model.getJobPosition())
                .address(buildAddress(
                        model.getAddressStreet(),
                        model.getAddressCity(),
                        model.getAddressState(),
                        model.getAddressZipCode(),
                        model.getAddressCountry()))
                .contactType(stringToEnum(model.getContactType(), ContactType.class))
                .isPrimary(model.getIsPrimary())
                .linkedInUrl(model.getLinkedInUrl())
                .twitterHandle(model.getTwitterHandle())
                .activeStatus(stringToEnum(model.getActiveStatus(), ActiveStatus.class))
                .notes(model.getNotes())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ContactModel toModel(ContactEntity entity) {
        if (entity == null) {
            return null;
        }

        return ContactModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .customerId(entity.getCustomerId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .jobPosition(entity.getJobPosition())
                .addressStreet(entity.getAddress() != null ? entity.getAddress().getStreet() : null)
                .addressCity(entity.getAddress() != null ? entity.getAddress().getCity() : null)
                .addressState(entity.getAddress() != null ? entity.getAddress().getState() : null)
                .addressZipCode(entity.getAddress() != null ? entity.getAddress().getZipCode() : null)
                .addressCountry(entity.getAddress() != null ? entity.getAddress().getCountry() : null)
                .contactType(enumToString(entity.getContactType()))
                .isPrimary(entity.getIsPrimary())
                .linkedInUrl(entity.getLinkedInUrl())
                .twitterHandle(entity.getTwitterHandle())
                .activeStatus(enumToString(entity.getActiveStatus()))
                .notes(entity.getNotes())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ContactEntity> toEntityList(List<ContactModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
