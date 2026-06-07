/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleAllocationCalendarProjection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemScheduleAllocationCalendarItemView(
        Long allocationId,
        Long workItemPlanId,
        Long workItemId,
        Long projectId,
        String key,
        String summary,
        Long assigneeId,
        String assigneeName,
        String assigneeAvatarUrl,
        Long start,
        Long end,
        Long effortMillis,
        Long plannedStart,
        Long plannedEnd,
        String source,
        Long sourceRunId,
        Long sourceRunItemId,
        Boolean locked,
        WorkItemCalendarSummaryViews.IssueTypeSummaryView issueType,
        WorkItemCalendarSummaryViews.StatusSummaryView status,
        WorkItemCalendarSummaryViews.PrioritySummaryView priority
) {

    public static WorkItemScheduleAllocationCalendarItemView from(WorkItemScheduleAllocationCalendarProjection projection) {
        return new WorkItemScheduleAllocationCalendarItemView(
                projection.allocationId(),
                projection.workItemPlanId(),
                projection.workItemId(),
                projection.projectId(),
                projection.key(),
                projection.summary(),
                projection.assigneeId(),
                projection.assigneeName(),
                projection.assigneeAvatarUrl(),
                projection.start(),
                projection.end(),
                projection.effortMillis(),
                projection.plannedStart(),
                projection.plannedEnd(),
                projection.source(),
                projection.sourceRunId(),
                projection.sourceRunItemId(),
                projection.locked(),
                new WorkItemCalendarSummaryViews.IssueTypeSummaryView(
                        projection.issueTypeId(),
                        projection.issueTypeName(),
                        projection.issueTypeIconUrl(),
                        projection.issueTypeHierarchyLevel()
                ),
                new WorkItemCalendarSummaryViews.StatusSummaryView(
                        projection.statusId(),
                        projection.statusName()
                ),
                new WorkItemCalendarSummaryViews.PrioritySummaryView(
                        projection.priorityId(),
                        projection.priorityName(),
                        projection.priorityColor()
                )
        );
    }
}
