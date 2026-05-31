/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunModel;

@Component
public class OptimizationRunMapper extends BaseMapper {
    public OptimizationRunModel toModel(OptimizationRunEntity entity) {
        if (entity == null) { return null; }
        return OptimizationRunModel.builder()
                .id(entity.getId()).tenantId(entity.getTenantId()).projectId(entity.getProjectId())
                .scope(entity.getScope()).objective(entity.getObjective()).changeScope(entity.getChangeScope())
                .status(entity.getStatus())
                .planningStart(longToLocalDateTime(entity.getPlanningStart()))
                .planningEnd(longToLocalDateTime(entity.getPlanningEnd()))
                .selectedWorkItemCount(entity.getSelectedWorkItemCount()).summaryJson(entity.getSummaryJson())
                .algorithmKey(entity.getAlgorithmKey()).algorithmVersion(entity.getAlgorithmVersion())
                .solverStatus(entity.getSolverStatus()).objectiveScore(entity.getObjectiveScore())
                .appliedAt(longToLocalDateTime(entity.getAppliedAt())).appliedBy(entity.getAppliedBy())
                .discardedAt(longToLocalDateTime(entity.getDiscardedAt()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt())).createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt())).updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt())).build();
    }

    public OptimizationRunEntity toEntity(OptimizationRunModel model) {
        if (model == null) { return null; }
        return OptimizationRunEntity.builder()
                .id(model.getId()).tenantId(model.getTenantId()).projectId(model.getProjectId())
                .scope(model.getScope()).objective(model.getObjective()).changeScope(model.getChangeScope())
                .status(model.getStatus())
                .planningStart(localDateTimeToLong(model.getPlanningStart()))
                .planningEnd(localDateTimeToLong(model.getPlanningEnd()))
                .selectedWorkItemCount(model.getSelectedWorkItemCount()).summaryJson(model.getSummaryJson())
                .algorithmKey(model.getAlgorithmKey()).algorithmVersion(model.getAlgorithmVersion())
                .solverStatus(model.getSolverStatus()).objectiveScore(model.getObjectiveScore())
                .appliedAt(localDateTimeToLong(model.getAppliedAt())).appliedBy(model.getAppliedBy())
                .discardedAt(localDateTimeToLong(model.getDiscardedAt()))
                .createdAt(localDateTimeToLong(model.getCreatedAt())).createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt())).updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt())).build();
    }
}
