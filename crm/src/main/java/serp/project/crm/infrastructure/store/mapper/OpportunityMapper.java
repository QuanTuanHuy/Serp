/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.entity.OpportunityEntity;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.infrastructure.store.model.OpportunityModel;

@Component
@RequiredArgsConstructor
public class OpportunityMapper extends BaseMapper {

    public OpportunityEntity toEntity(OpportunityModel model) {
        if (model == null) {
            return null;
        }

        return OpportunityEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .leadId(model.getLeadId())
                .customerId(model.getCustomerId())
                .stage(stringToEnum(model.getStage(), OpportunityStage.class))
                .estimatedValue(model.getEstimatedValue())
                .probability(model.getProbability())
                .expectedCloseDate(model.getExpectedCloseDate())
                .actualCloseDate(model.getActualCloseDate())
                .assignedTo(model.getAssignedTo())
                .notes(model.getNotes())
                .lossReason(model.getLossReason())
                .createdAt(toTimestamp(model.getCreatedAt()))
                .updatedAt(toTimestamp(model.getUpdatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public OpportunityModel toModel(OpportunityEntity entity) {
        if (entity == null) {
            return null;
        }

        return OpportunityModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .leadId(entity.getLeadId())
                .customerId(entity.getCustomerId())
                .stage(enumToString(entity.getStage()))
                .estimatedValue(entity.getEstimatedValue())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .actualCloseDate(entity.getActualCloseDate())
                .assignedTo(entity.getAssignedTo())
                .notes(entity.getNotes())
                .lossReason(entity.getLossReason())
                .createdAt(toLocalDateTime(entity.getCreatedAt()))
                .updatedAt(toLocalDateTime(entity.getUpdatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<OpportunityEntity> toEntityList(List<OpportunityModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream().map(this::toEntity).toList();
    }
}
