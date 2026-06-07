/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record ResourceWorkloadPlan(
        Long id,
        Long workItemId,
        Long plannedStart,
        Long plannedEnd
) {
}
