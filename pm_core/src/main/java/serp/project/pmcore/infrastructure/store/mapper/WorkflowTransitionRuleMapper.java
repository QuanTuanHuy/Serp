/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowTransitionRuleModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkflowTransitionRuleMapper extends BaseMapper {

    public WorkflowTransitionRuleModel toModel(WorkflowTransitionRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowTransitionRuleModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .transitionId(entity.getTransitionId())
                .ruleStage(entity.getRuleStage())
                .ruleKey(entity.getRuleKey())
                .configJson(entity.getConfigJson())
                .sequence(entity.getSequence())
                .isEnabled(entity.getIsEnabled())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkflowTransitionRuleEntity toEntity(WorkflowTransitionRuleModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowTransitionRuleEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .transitionId(model.getTransitionId())
                .ruleStage(model.getRuleStage())
                .ruleKey(model.getRuleKey())
                .configJson(model.getConfigJson())
                .sequence(model.getSequence())
                .isEnabled(model.getIsEnabled())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkflowTransitionRuleEntity> toEntities(List<WorkflowTransitionRuleModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkflowTransitionRuleModel> toModels(List<WorkflowTransitionRuleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
