/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunItemModel;

import java.util.Collections;
import java.util.List;

@Component
public class OptimizationRunItemMapper extends BaseMapper {
    public OptimizationRunItemModel toModel(OptimizationRunItemEntity e) {
        if (e == null) { return null; }
        return OptimizationRunItemModel.builder()
                .id(e.getId()).tenantId(e.getTenantId()).runId(e.getRunId()).projectId(e.getProjectId())
                .workItemId(e.getWorkItemId()).workItemUpdatedAtSnapshot(longToLocalDateTime(e.getWorkItemUpdatedAtSnapshot()))
                .planUpdatedAtSnapshot(longToLocalDateTime(e.getPlanUpdatedAtSnapshot()))
                .currentAssigneeId(e.getCurrentAssigneeId()).suggestedAssigneeId(e.getSuggestedAssigneeId())
                .overrideAssigneeId(e.getOverrideAssigneeId()).currentPlannedStart(longToLocalDateTime(e.getCurrentPlannedStart()))
                .currentPlannedEnd(longToLocalDateTime(e.getCurrentPlannedEnd())).suggestedPlannedStart(longToLocalDateTime(e.getSuggestedPlannedStart()))
                .suggestedPlannedEnd(longToLocalDateTime(e.getSuggestedPlannedEnd())).overridePlannedStart(longToLocalDateTime(e.getOverridePlannedStart()))
                .overridePlannedEnd(longToLocalDateTime(e.getOverridePlannedEnd())).currentDueDate(longToLocalDateTime(e.getCurrentDueDate()))
                .assignmentDecision(e.getAssignmentDecision()).scheduleDecision(e.getScheduleDecision())
                .assignmentApplyStatus(e.getAssignmentApplyStatus()).scheduleApplyStatus(e.getScheduleApplyStatus())
                .score(e.getScore()).cost(e.getCost()).confidence(e.getConfidence())
                .assignmentReasonsJson(e.getAssignmentReasonsJson()).scheduleReasonsJson(e.getScheduleReasonsJson())
                .violationsJson(e.getViolationsJson()).appliedAt(longToLocalDateTime(e.getAppliedAt()))
                .assignmentSkippedReason(e.getAssignmentSkippedReason()).scheduleSkippedReason(e.getScheduleSkippedReason())
                .createdAt(longToLocalDateTime(e.getCreatedAt())).createdBy(e.getCreatedBy())
                .updatedAt(longToLocalDateTime(e.getUpdatedAt())).updatedBy(e.getUpdatedBy())
                .deletedAt(longToLocalDateTime(e.getDeletedAt())).build();
    }

    public OptimizationRunItemEntity toEntity(OptimizationRunItemModel m) {
        if (m == null) { return null; }
        return OptimizationRunItemEntity.builder()
                .id(m.getId()).tenantId(m.getTenantId()).runId(m.getRunId()).projectId(m.getProjectId())
                .workItemId(m.getWorkItemId()).workItemUpdatedAtSnapshot(localDateTimeToLong(m.getWorkItemUpdatedAtSnapshot()))
                .planUpdatedAtSnapshot(localDateTimeToLong(m.getPlanUpdatedAtSnapshot()))
                .currentAssigneeId(m.getCurrentAssigneeId()).suggestedAssigneeId(m.getSuggestedAssigneeId())
                .overrideAssigneeId(m.getOverrideAssigneeId()).currentPlannedStart(localDateTimeToLong(m.getCurrentPlannedStart()))
                .currentPlannedEnd(localDateTimeToLong(m.getCurrentPlannedEnd())).suggestedPlannedStart(localDateTimeToLong(m.getSuggestedPlannedStart()))
                .suggestedPlannedEnd(localDateTimeToLong(m.getSuggestedPlannedEnd())).overridePlannedStart(localDateTimeToLong(m.getOverridePlannedStart()))
                .overridePlannedEnd(localDateTimeToLong(m.getOverridePlannedEnd())).currentDueDate(localDateTimeToLong(m.getCurrentDueDate()))
                .assignmentDecision(m.getAssignmentDecision()).scheduleDecision(m.getScheduleDecision())
                .assignmentApplyStatus(m.getAssignmentApplyStatus()).scheduleApplyStatus(m.getScheduleApplyStatus())
                .score(m.getScore()).cost(m.getCost()).confidence(m.getConfidence())
                .assignmentReasonsJson(m.getAssignmentReasonsJson()).scheduleReasonsJson(m.getScheduleReasonsJson())
                .violationsJson(m.getViolationsJson()).appliedAt(localDateTimeToLong(m.getAppliedAt()))
                .assignmentSkippedReason(m.getAssignmentSkippedReason()).scheduleSkippedReason(m.getScheduleSkippedReason())
                .createdAt(localDateTimeToLong(m.getCreatedAt())).createdBy(m.getCreatedBy())
                .updatedAt(localDateTimeToLong(m.getUpdatedAt())).updatedBy(m.getUpdatedBy())
                .deletedAt(localDateTimeToLong(m.getDeletedAt())).build();
    }

    public List<OptimizationRunItemEntity> toEntities(List<OptimizationRunItemModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }

    public List<OptimizationRunItemModel> toModels(List<OptimizationRunItemEntity> entities) {
        if (entities == null || entities.isEmpty()) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).toList();
    }
}
