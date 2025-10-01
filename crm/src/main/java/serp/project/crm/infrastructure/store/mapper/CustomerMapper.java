/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.CustomerEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.infrastructure.store.model.CustomerModel;

@Component
@RequiredArgsConstructor
public class CustomerMapper extends BaseMapper {

    public CustomerEntity toEntity(CustomerModel model) {
        if (model == null) {
            return null;
        }

        return CustomerEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .phone(model.getPhone())
                .email(model.getEmail())
                .website(model.getWebsite())
                .industry(model.getIndustry())
                .companySize(model.getCompanySize())
                .parentCustomerId(model.getParentCustomerId())
                .taxId(model.getTaxId())
                .creditLimit(model.getCreditLimit())
                .totalOpportunities(model.getTotalOpportunities())
                .wonOpportunities(model.getWonOpportunities())
                .totalRevenue(model.getTotalRevenue())
                .activeStatus(stringToEnum(model.getActiveStatus(), ActiveStatus.class))
                .notes(model.getNotes())
                .address(buildAddress(
                        model.getAddressStreet(),
                        model.getAddressCity(),
                        model.getAddressState(),
                        model.getAddressZipCode(),
                        model.getAddressCountry()))
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public CustomerModel toModel(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        return CustomerModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .parentCustomerId(entity.getParentCustomerId())
                .taxId(entity.getTaxId())
                .creditLimit(entity.getCreditLimit())
                .totalOpportunities(entity.getTotalOpportunities())
                .wonOpportunities(entity.getWonOpportunities())
                .totalRevenue(entity.getTotalRevenue())
                .activeStatus(enumToString(entity.getActiveStatus()))
                .notes(entity.getNotes())
                .addressStreet(entity.getAddress() != null ? entity.getAddress().getStreet() : null)
                .addressCity(entity.getAddress() != null ? entity.getAddress().getCity() : null)
                .addressState(entity.getAddress() != null ? entity.getAddress().getState() : null)
                .addressZipCode(entity.getAddress() != null ? entity.getAddress().getZipCode() : null)
                .addressCountry(entity.getAddress() != null ? entity.getAddress().getCountry() : null)
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomerEntity> toEntityList(List<CustomerModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
