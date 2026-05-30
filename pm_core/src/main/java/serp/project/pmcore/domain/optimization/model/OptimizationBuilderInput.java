/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import java.util.List;

public record OptimizationBuilderInput(
        Long tenantId,
        Long projectId,
        List<Long> selectedWorkItemIds,
        Long planningStart,
        Long planningEnd,
        OptimizationRunIntent intent
) {
}
