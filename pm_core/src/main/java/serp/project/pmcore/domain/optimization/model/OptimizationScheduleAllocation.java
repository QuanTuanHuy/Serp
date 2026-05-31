/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

public record OptimizationScheduleAllocation(
        Long assigneeId,
        Long start,
        Long end,
        Long effortMillis
) {
}
