/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemTimelineItemView(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        Long assigneeId,
        Long startDate,
        Long dueDate,
        boolean isUnscheduled,
        boolean hasChildren,
        String rank,
        ScheduleSummaryView schedule,
        IssueTypeSummaryView issueType,
        StatusSummaryView status,
        PrioritySummaryView priority
) {

    public static WorkItemTimelineItemView from(WorkItemTimelineItemProjection projection,
                                                WorkItemPlanEntity plan,
                                                List<WorkItemPlanAllocationEntity> allocations) {
        return new WorkItemTimelineItemView(
                projection.id(),
                projection.projectId(),
                projection.parentId(),
                projection.key(),
                projection.summary(),
                projection.assigneeId(),
                projection.startDate(),
                projection.dueDate(),
                plan == null,
                projection.hasChildren(),
                projection.rank(),
                plan == null ? null : new ScheduleSummaryView(
                        plan.getPlannedStart(),
                        plan.getPlannedEnd(),
                        plan.getSource() == null ? null : plan.getSource().name(),
                        plan.getLocked(),
                        plan.getSourceRunId(),
                        toAllocationViews(allocations)
                ),
                new IssueTypeSummaryView(
                        projection.issueTypeId(),
                        projection.issueTypeName(),
                        projection.issueTypeIconUrl(),
                        projection.issueTypeHierarchyLevel()
                ),
                new StatusSummaryView(
                        projection.statusId(),
                        projection.statusName()
                ),
                new PrioritySummaryView(
                        projection.priorityId(),
                        projection.priorityName(),
                        projection.priorityColor()
                )
        );
    }

    public static WorkItemTimelineItemView from(WorkItemTimelineItemProjection projection,
                                                WorkItemPlanEntity plan) {
        return from(projection, plan, List.of());
    }

    private static List<ScheduleAllocationView> toAllocationViews(List<WorkItemPlanAllocationEntity> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return List.of();
        }
        return allocations.stream()
                .map(allocation -> new ScheduleAllocationView(
                        allocation.getAssigneeId(),
                        allocation.getStartTime(),
                        allocation.getEndTime(),
                        allocation.getEffortMillis()
                ))
                .toList();
    }

    public record IssueTypeSummaryView(
            Long id,
            String name,
            String iconUrl,
            Integer hierarchyLevel
    ) {
    }

    public record StatusSummaryView(
            Long id,
            String name
    ) {
    }

    public record PrioritySummaryView(
            Long id,
            String name,
            String color
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ScheduleSummaryView(
            Long plannedStart,
            Long plannedEnd,
            String source,
            Boolean locked,
            Long sourceRunId,
            List<ScheduleAllocationView> allocations
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ScheduleAllocationView(
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis
    ) {
    }
}
