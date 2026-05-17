/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.infrastructure.store.model.OptimizationRunWarningModel;

import java.util.Collections;
import java.util.List;

@Component
public class OptimizationRunWarningMapper extends BaseMapper {
    public OptimizationRunWarningModel toModel(OptimizationRunWarningEntity e) {
        if (e == null) { return null; }
        return OptimizationRunWarningModel.builder()
                .id(e.getId()).tenantId(e.getTenantId()).runId(e.getRunId()).workItemId(e.getWorkItemId())
                .severity(e.getSeverity()).code(e.getCode()).message(e.getMessage()).detailsJson(e.getDetailsJson())
                .createdAt(longToLocalDateTime(e.getCreatedAt())).createdBy(e.getCreatedBy())
                .updatedAt(longToLocalDateTime(e.getUpdatedAt())).updatedBy(e.getUpdatedBy())
                .deletedAt(longToLocalDateTime(e.getDeletedAt())).build();
    }

    public OptimizationRunWarningEntity toEntity(OptimizationRunWarningModel m) {
        if (m == null) { return null; }
        return OptimizationRunWarningEntity.builder()
                .id(m.getId()).tenantId(m.getTenantId()).runId(m.getRunId()).workItemId(m.getWorkItemId())
                .severity(m.getSeverity()).code(m.getCode()).message(m.getMessage()).detailsJson(m.getDetailsJson())
                .createdAt(localDateTimeToLong(m.getCreatedAt())).createdBy(m.getCreatedBy())
                .updatedAt(localDateTimeToLong(m.getUpdatedAt())).updatedBy(m.getUpdatedBy())
                .deletedAt(localDateTimeToLong(m.getDeletedAt())).build();
    }

    public List<OptimizationRunWarningEntity> toEntities(List<OptimizationRunWarningModel> models) {
        if (models == null || models.isEmpty()) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).toList();
    }

    public List<OptimizationRunWarningModel> toModels(List<OptimizationRunWarningEntity> entities) {
        if (entities == null || entities.isEmpty()) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).toList();
    }
}
