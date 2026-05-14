/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;

public record UpdateWorkItemResult(
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
        Long startDate,
        Long dueDate,
        String rank,
        Long timeOriginalEstimate,
        Long timeRemainingEstimate,
        Long timeSpent,
        List<String> changedFields,
        Long updatedAt,
        Long updatedBy
) {

    public static UpdateWorkItemResult from(WorkItemEntity entity, List<String> changedFields) {
        return new UpdateWorkItemResult(
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
                entity.getStartDate(),
                entity.getDueDate(),
                entity.getRank(),
                entity.getTimeOriginalEstimate(),
                entity.getTimeRemainingEstimate(),
                entity.getTimeSpent(),
                changedFields,
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
