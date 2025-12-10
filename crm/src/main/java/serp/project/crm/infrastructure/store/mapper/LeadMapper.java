/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.LeadEntity;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;
import serp.project.crm.infrastructure.store.model.LeadModel;

@Component
@RequiredArgsConstructor
public class LeadMapper extends BaseMapper {

    public LeadEntity toEntity(LeadModel model) {
        if (model == null) {
            return null;
        }

        return LeadEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .company(model.getCompany())
                .industry(model.getIndustry())
                .companySize(model.getCompanySize())
                .website(model.getWebsite())
                .name(model.getName())
                .email(model.getEmail())
                .phone(model.getPhone())
                .jobTitle(model.getJobTitle())
                .address(buildAddress(
                        model.getAddressStreet(),
                        model.getAddressCity(),
                        model.getAddressState(),
                        model.getAddressZipCode(),
                        model.getAddressCountry()))
                .leadSource(stringToEnum(model.getLeadSource(), LeadSource.class))
                .leadStatus(stringToEnum(model.getLeadStatus(), LeadStatus.class))
                .assignedTo(model.getAssignedTo())
                .estimatedValue(model.getEstimatedValue())
                .probability(model.getProbability())
                .expectedCloseDate(model.getExpectedCloseDate())
                .notes(model.getNotes())
                .convertedOpportunityId(model.getConvertedOpportunityId())
                .convertedCustomerId(model.getConvertedCustomerId())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public LeadModel toModel(LeadEntity entity) {
        if (entity == null) {
            return null;
        }

        return LeadModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .company(entity.getCompany())
                .industry(entity.getIndustry())
                .companySize(entity.getCompanySize())
                .website(entity.getWebsite())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .jobTitle(entity.getJobTitle())
                .addressStreet(entity.getAddress() != null ? entity.getAddress().getStreet() : null)
                .addressCity(entity.getAddress() != null ? entity.getAddress().getCity() : null)
                .addressState(entity.getAddress() != null ? entity.getAddress().getState() : null)
                .addressZipCode(entity.getAddress() != null ? entity.getAddress().getZipCode() : null)
                .addressCountry(entity.getAddress() != null ? entity.getAddress().getCountry() : null)
                .leadSource(enumToString(entity.getLeadSource()))
                .leadStatus(enumToString(entity.getLeadStatus()))
                .assignedTo(entity.getAssignedTo())
                .estimatedValue(entity.getEstimatedValue())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .notes(entity.getNotes())
                .convertedOpportunityId(entity.getConvertedOpportunityId())
                .convertedCustomerId(entity.getConvertedCustomerId())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<LeadEntity> toEntityList(List<LeadModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
