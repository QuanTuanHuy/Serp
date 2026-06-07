/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemScheduleAllocationCalendarProjection(
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
        Long issueTypeId,
        String issueTypeName,
        String issueTypeIconUrl,
        Integer issueTypeHierarchyLevel,
        Long statusId,
        String statusName,
        Long priorityId,
        String priorityName,
        String priorityColor
) {
}
