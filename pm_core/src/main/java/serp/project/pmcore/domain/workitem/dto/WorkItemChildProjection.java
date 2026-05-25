/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record WorkItemChildProjection(
        Long id,
        Long projectId,
        Long parentId,
        String key,
        String summary,
        Long assigneeId,
        Long issueTypeId,
        String issueTypeName,
        String issueTypeIconUrl,
        Integer issueTypeHierarchyLevel,
        Long statusId,
        String statusKey,
        String statusName,
        Long priorityId,
        String priorityName,
        String priorityColor,
        String rank
) {
}
