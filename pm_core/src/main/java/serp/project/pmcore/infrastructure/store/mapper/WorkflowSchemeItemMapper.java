/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowSchemeItemMapper extends BaseMapper {

    public WorkflowSchemeItemEntity toEntity(WorkflowSchemeItemModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowSchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .issueTypeId(model.getIssueTypeId())
                .workflowId(model.getWorkflowId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public WorkflowSchemeItemModel toModel(WorkflowSchemeItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowSchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .issueTypeId(entity.getIssueTypeId())
                .workflowId(entity.getWorkflowId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<WorkflowSchemeItemEntity> toEntities(List<WorkflowSchemeItemModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkflowSchemeItemModel> toModels(List<WorkflowSchemeItemEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
