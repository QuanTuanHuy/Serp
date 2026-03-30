/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemDetailView(
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

        IssueTypeSummaryView issueType,
        UserSummaryView assignee,
        UserSummaryView reporter,
        WorkflowStepSummaryView workflowStep,
        StatusSummaryView status,
        PrioritySummaryView priority,

        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {

    public static WorkItemDetailView from(WorkItemEntity workItem,
                                          IssueTypeEntity issueType,
                                          StatusEntity status,
                                          PriorityEntity priority,
                                          WorkflowStepEntity workflowStep) {
        return new WorkItemDetailView(
                workItem.getId(),
                workItem.getProjectId(),
                workItem.getIssueTypeId(),
                workItem.getIssueNo(),
                workItem.getKey(),
                workItem.getSummary(),
                workItem.getDescription(),
                workItem.getWorkflowStepId(),
                workItem.getStatusId(),
                workItem.getPriorityId(),
                workItem.getResolutionId(),
                workItem.getAssigneeId(),
                workItem.getReporterId(),
                workItem.getParentId(),
                workItem.getSecurityLevelId(),
                workItem.getDueDate(),
                workItem.getRank(),
                workItem.getTimeOriginalEstimate(),
                workItem.getTimeRemainingEstimate(),
                workItem.getTimeSpent(),
                new IssueTypeSummaryView(issueType.getId(), issueType.getName()),
                new UserSummaryView(workItem.getAssigneeId(), null),
                new UserSummaryView(workItem.getReporterId(), null),
                new WorkflowStepSummaryView(workflowStep.getId(), workflowStep.getName()),
                new StatusSummaryView(status.getId(), status.getName()),
                new PrioritySummaryView(priority.getId(), priority.getName(), priority.getColor()),
                workItem.getCreatedAt(),
                workItem.getCreatedBy(),
                workItem.getUpdatedAt(),
                workItem.getUpdatedBy()
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record IssueTypeSummaryView(
            Long id,
            String name
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UserSummaryView(
            Long id,
            String displayName
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WorkflowStepSummaryView(
            Long id,
            String name
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatusSummaryView(
            Long id,
            String name
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PrioritySummaryView(
            Long id,
            String name,
            String color
    ) {
    }
}
