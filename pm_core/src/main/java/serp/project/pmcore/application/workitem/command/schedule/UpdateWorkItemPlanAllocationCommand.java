/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.schedule;

public record UpdateWorkItemPlanAllocationCommand(
        Long assigneeId,
        Long start,
        Long end,
        Long effortMillis
) {
}
