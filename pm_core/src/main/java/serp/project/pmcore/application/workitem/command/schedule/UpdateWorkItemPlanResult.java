/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.schedule;

import lombok.Builder;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;

import java.util.List;

@Builder
public record UpdateWorkItemPlanResult(
        Long id,
        Long workItemId,
        Long projectId,
        Long plannedStart,
        Long plannedEnd,
        String source,
        Boolean locked,
        List<AllocationView> allocations
) {
    public static UpdateWorkItemPlanResult from(WorkItemPlanEntity plan,
                                                List<WorkItemPlanAllocationEntity> allocations) {
        return UpdateWorkItemPlanResult.builder()
                .id(plan.getId())
                .workItemId(plan.getWorkItemId())
                .projectId(plan.getProjectId())
                .plannedStart(plan.getPlannedStart())
                .plannedEnd(plan.getPlannedEnd())
                .source(plan.getSource() == null ? null : plan.getSource().name())
                .locked(plan.getLocked())
                .allocations(allocations == null
                        ? List.of()
                        : allocations.stream().map(AllocationView::from).toList())
                .build();
    }

    @Builder
    public record AllocationView(
            Long id,
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis
    ) {
        public static AllocationView from(WorkItemPlanAllocationEntity allocation) {
            return AllocationView.builder()
                    .id(allocation.getId())
                    .assigneeId(allocation.getAssigneeId())
                    .start(allocation.getStartTime())
                    .end(allocation.getEndTime())
                    .effortMillis(allocation.getEffortMillis())
                    .build();
        }
    }
}
