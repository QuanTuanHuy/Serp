/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record ResourceWorkloadAllocation(
        Long workItemPlanId,
        Long workItemId,
        Long assigneeId,
        Long startTime,
        Long endTime,
        Long effortMillis
) {
}
