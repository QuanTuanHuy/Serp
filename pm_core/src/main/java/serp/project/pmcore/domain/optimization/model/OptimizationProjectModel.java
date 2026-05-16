/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

import java.util.List;
import java.util.Map;

public record OptimizationProjectModel(
        Long tenantId,
        Long projectId,
        ProjectEntity project,
        Long planningStart,
        Long planningEnd,
        OptimizationDependencyGraph dependencyGraph,
        List<OptimizationWorkItem> workItems,
        List<ResourceCapacitySlot> capacitySlots,
        List<OptimizationConstraintViolation> warnings,
        Map<Long, Long> earliestStartByWorkItemId
) {
}
