/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.Builder;

@Builder
public record WorkItemBoardItemProjection(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        String description,
        Long assigneeId,
        String assigneeName,
        String assigneeAvatarUrl,
        Long reporterId,
        Long startDate,
        Long dueDate,
        String rank,
        Long issueTypeId,
        String issueTypeName,
        String issueTypeIconUrl,
        Integer issueTypeHierarchyLevel,
        Long statusId,
        String statusKey,
        String statusName,
        Long priorityId,
        String priorityName,
        String priorityIconUrl,
        String priorityColor
) {
}
