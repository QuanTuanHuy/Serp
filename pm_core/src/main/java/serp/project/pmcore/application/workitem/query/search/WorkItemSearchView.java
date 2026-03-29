/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.search;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public record WorkItemSearchView(
        Long id,
        Long projectId,
        Long issueTypeId,
        Long issueNo,
        String key,
        String summary,
        String description,
        Long workflowStepId,
        Long statusId,
        Long priorityId,
        Long resolutionId,
        Long assigneeId,
        Long reporterId,
        Long parentId,
        Long securityLevelId,
        Long dueDate,
        String rank,
        Long timeOriginalEstimate,
        Long timeRemainingEstimate,
        Long timeSpent,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy,
        String issueTypeName,
        String issueTypeIconUrl,
        Integer issueTypeHierarchyLevel,
        String priorityName,
        String priorityIconUrl,
        String priorityColor,
        Integer prioritySequence
) {
    public static WorkItemSearchView from(WorkItemEntity entity) {
        return new WorkItemSearchView(
                entity.getId(),
                entity.getProjectId(),
                entity.getIssueTypeId(),
                entity.getIssueNo(),
                entity.getKey(),
                entity.getSummary(),
                entity.getDescription(),
                entity.getWorkflowStepId(),
                entity.getStatusId(),
                entity.getPriorityId(),
                entity.getResolutionId(),
                entity.getAssigneeId(),
                entity.getReporterId(),
                entity.getParentId(),
                entity.getSecurityLevelId(),
                entity.getDueDate(),
                entity.getRank(),
                entity.getTimeOriginalEstimate(),
                entity.getTimeRemainingEstimate(),
                entity.getTimeSpent(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy(),
                entity.getIssueTypeName(),
                entity.getIssueTypeIconUrl(),
                entity.getIssueTypeHierarchyLevel(),
                entity.getPriorityName(),
                entity.getPriorityIconUrl(),
                entity.getPriorityColor(),
                entity.getPrioritySequence()
        );
    }
}
