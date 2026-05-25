/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemTimelineItemProjection(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        Long assigneeId,
        Long startDate,
        Long dueDate,
        boolean unscheduled,
        boolean hasChildren,
        String rank,
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
