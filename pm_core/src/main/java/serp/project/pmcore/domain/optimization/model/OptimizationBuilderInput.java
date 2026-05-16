/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.optimization.enums.OptimizationMode;

import java.util.List;

public record OptimizationBuilderInput(
        Long tenantId,
        Long projectId,
        List<Long> selectedWorkItemIds,
        Long planningStart,
        Long planningEnd,
        Boolean allowReassignment,
        Boolean allowScheduleChanges,
        OptimizationMode mode
) {
}
