/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkItemMapper extends BaseMapper {
    public WorkItemModel toModel(WorkItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .projectId(entity.getProjectId())
                .issueTypeId(entity.getIssueTypeId())
                .issueNo(entity.getIssueNo())
                .key(entity.getKey())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .statusId(entity.getStatusId())
                .priorityId(entity.getPriorityId())
                .assigneeId(entity.getAssigneeId())
                .reporterId(entity.getReporterId())
                .parentId(entity.getParentId())
                .resolutionId(entity.getResolutionId())
                .dueDate(longToLocalDateTime(entity.getDueDate()))
                .rank(entity.getRank())
                .timeOriginalEstimate(entity.getTimeOriginalEstimate())
                .timeRemainingEstimate(entity.getTimeRemainingEstimate())
                .timeSpent(entity.getTimeSpent())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public WorkItemEntity toEntity(WorkItemModel model) {
        if (model == null) {
            return null;
        }
        return WorkItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .projectId(model.getProjectId())
                .issueTypeId(model.getIssueTypeId())
                .issueNo(model.getIssueNo())
                .key(model.getKey())
                .summary(model.getSummary())
                .description(model.getDescription())
                .statusId(model.getStatusId())
                .priorityId(model.getPriorityId())
                .assigneeId(model.getAssigneeId())
                .reporterId(model.getReporterId())
                .parentId(model.getParentId())
                .resolutionId(model.getResolutionId())
                .dueDate(localDateTimeToLong(model.getDueDate()))
                .rank(model.getRank())
                .timeOriginalEstimate(model.getTimeOriginalEstimate())
                .timeRemainingEstimate(model.getTimeRemainingEstimate())
                .timeSpent(model.getTimeSpent())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<WorkItemEntity> toEntities(List<WorkItemModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
