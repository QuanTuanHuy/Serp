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
                .accountId(model.getAccountId())
                .stage(stringToEnum(model.getStage(), OpportunityStage.class))
                .estimatedValue(model.getEstimatedValue())
                .actualValue(model.getActualValue())
                .probability(model.getProbability())
                .expectedCloseDate(model.getExpectedCloseDate())
                .actualCloseDate(model.getActualCloseDate())
                .assignedTo(model.getAssignedTo())
                .notes(model.getNotes())
                .lossReason(model.getLossReason())
                .reopenReason(model.getReopenReason())
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
                .accountId(entity.getAccountId())
                .stage(enumToString(entity.getStage()))
                .estimatedValue(entity.getEstimatedValue())
                .actualValue(entity.getActualValue())
                .probability(entity.getProbability())
                .expectedCloseDate(entity.getExpectedCloseDate())
                .actualCloseDate(entity.getActualCloseDate())
                .assignedTo(entity.getAssignedTo())
                .notes(entity.getNotes())
                .lossReason(entity.getLossReason())
                .reopenReason(entity.getReopenReason())
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
