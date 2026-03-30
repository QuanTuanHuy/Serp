/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
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
                                          WorkflowStepEntity workflowStep,
                                          String assigneeName,
                                          String reporterName) {

        return WorkItemDetailView.builder()
                .id(workItem.getId())
                .projectId(workItem.getProjectId())
                .issueNo(workItem.getIssueNo())
                .key(workItem.getKey())
                .summary(workItem.getSummary())
                .description(workItem.getDescription())
                .resolutionId(workItem.getResolutionId())
                .parentId(workItem.getParentId())
                .securityLevelId(workItem.getSecurityLevelId())
                .dueDate(workItem.getDueDate())
                .rank(workItem.getRank())
                .timeOriginalEstimate(workItem.getTimeOriginalEstimate())
                .timeRemainingEstimate(workItem.getTimeRemainingEstimate())
                .timeSpent(workItem.getTimeSpent())

                .issueType(issueType != null ? new IssueTypeSummaryView(issueType.getId(), issueType.getName()) : null)
                .workflowStep(workflowStep != null ? new WorkflowStepSummaryView(workflowStep.getId(), workflowStep.getName()) : null)
                .status(status != null ? new StatusSummaryView(status.getId(), status.getName()) : null)
                .priority(priority != null ? new PrioritySummaryView(priority.getId(), priority.getName(), priority.getColor()) : null)

                .assignee(workItem.getAssigneeId() != null ? new UserSummaryView(workItem.getAssigneeId(), assigneeName) : null)
                .reporter(workItem.getReporterId() != null ? new UserSummaryView(workItem.getReporterId(), reporterName) : null)

                .createdAt(workItem.getCreatedAt())
                .createdBy(workItem.getCreatedBy())
                .updatedAt(workItem.getUpdatedAt())
                .updatedBy(workItem.getUpdatedBy())
                .build();
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
