/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanAllocationModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkItemPlanAllocationMapper extends BaseMapper {
    public WorkItemPlanAllocationModel toModel(WorkItemPlanAllocationEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkItemPlanAllocationModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .projectId(entity.getProjectId())
                .workItemPlanId(entity.getWorkItemPlanId())
                .workItemId(entity.getWorkItemId())
                .assigneeId(entity.getAssigneeId())
                .startTime(longToLocalDateTime(entity.getStartTime()))
                .endTime(longToLocalDateTime(entity.getEndTime()))
                .effortMillis(entity.getEffortMillis())
                .source(entity.getSource())
                .sourceRunId(entity.getSourceRunId())
                .sourceRunItemId(entity.getSourceRunItemId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkItemPlanAllocationEntity toEntity(WorkItemPlanAllocationModel model) {
        if (model == null) {
            return null;
        }
        return WorkItemPlanAllocationEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .projectId(model.getProjectId())
                .workItemPlanId(model.getWorkItemPlanId())
                .workItemId(model.getWorkItemId())
                .assigneeId(model.getAssigneeId())
                .startTime(localDateTimeToLong(model.getStartTime()))
                .endTime(localDateTimeToLong(model.getEndTime()))
                .effortMillis(model.getEffortMillis())
                .source(model.getSource())
                .sourceRunId(model.getSourceRunId())
                .sourceRunItemId(model.getSourceRunItemId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkItemPlanAllocationEntity> toEntities(List<WorkItemPlanAllocationModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkItemPlanAllocationModel> toModels(List<WorkItemPlanAllocationEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
