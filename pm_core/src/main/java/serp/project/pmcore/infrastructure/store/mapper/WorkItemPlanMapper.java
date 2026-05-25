/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkItemPlanMapper extends BaseMapper {
    public WorkItemPlanModel toModel(WorkItemPlanEntity entity) {
        if (entity == null) { return null; }
        return WorkItemPlanModel.builder()
                .id(entity.getId()).tenantId(entity.getTenantId()).projectId(entity.getProjectId())
                .workItemId(entity.getWorkItemId()).plannedStart(longToLocalDateTime(entity.getPlannedStart()))
                .plannedEnd(longToLocalDateTime(entity.getPlannedEnd())).source(entity.getSource())
                .sourceRunId(entity.getSourceRunId()).locked(entity.getLocked())
                .createdAt(longToLocalDateTime(entity.getCreatedAt())).createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt())).updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt())).build();
    }

    public WorkItemPlanEntity toEntity(WorkItemPlanModel model) {
        if (model == null) { return null; }
        return WorkItemPlanEntity.builder()
                .id(model.getId()).tenantId(model.getTenantId()).projectId(model.getProjectId())
                .workItemId(model.getWorkItemId()).plannedStart(localDateTimeToLong(model.getPlannedStart()))
                .plannedEnd(localDateTimeToLong(model.getPlannedEnd())).source(model.getSource())
                .sourceRunId(model.getSourceRunId()).locked(model.getLocked())
                .createdAt(localDateTimeToLong(model.getCreatedAt())).createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt())).updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt())).build();
    }

    public List<WorkItemPlanEntity> toEntities(List<WorkItemPlanModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }
}
