/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkflowSchemeMapper extends BaseMapper {

    public WorkflowSchemeEntity toEntity(WorkflowSchemeModel model) {
        if (model == null) {
            return null;
        }
        return WorkflowSchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultWorkflowId(model.getDefaultWorkflowId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public WorkflowSchemeModel toModel(WorkflowSchemeEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkflowSchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultWorkflowId(entity.getDefaultWorkflowId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<WorkflowSchemeEntity> toEntities(List<WorkflowSchemeModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
