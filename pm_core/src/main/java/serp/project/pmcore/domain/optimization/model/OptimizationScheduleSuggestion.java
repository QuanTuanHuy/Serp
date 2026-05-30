/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;

import java.util.List;

public record OptimizationScheduleSuggestion(
        Long workItemId,
        Long assigneeId,
        Long plannedStart,
        Long plannedEnd,
        Long allocatedEffortMillis,
        List<OptimizationScheduleAllocation> allocations,
        OptimizationConfidence confidence,
        List<String> reasons,
        List<OptimizationConstraintViolation> violations
) {
}
