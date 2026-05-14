/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;

import java.time.Instant;

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

        Long startDate,
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

    public static WorkItemDetailView from(WorkItemDetailProjection workItem) {

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
                .startDate(instantToEpochMilli(workItem.getStartDate()))
                .dueDate(instantToEpochMilli(workItem.getDueDate()))
                .rank(workItem.getRank())
                .timeOriginalEstimate(workItem.getTimeOriginalEstimate())
                .timeRemainingEstimate(workItem.getTimeRemainingEstimate())
                .timeSpent(workItem.getTimeSpent())

                .issueType(workItem.getIssueTypeId() != null ?
                        new WorkItemDetailView.IssueTypeSummaryView(workItem.getIssueTypeId(), workItem.getIssueTypeName()) : null)
                .priority(workItem.getPriorityId() != null ?
                        new WorkItemDetailView.PrioritySummaryView(workItem.getPriorityId(), workItem.getPriorityName(), workItem.getPriorityColor()) : null)
                .status(workItem.getStatusId() != null ?
                        new WorkItemDetailView.StatusSummaryView(workItem.getStatusId(), workItem.getStatusName()) : null)
                .workflowStep(workItem.getWorkflowStepId() != null ?
                        new WorkItemDetailView.WorkflowStepSummaryView(workItem.getWorkflowStepId(), workItem.getWorkflowStepName()) : null)

                .assignee(workItem.getAssigneeId() != null ?
                        new WorkItemDetailView.UserSummaryView(workItem.getAssigneeId(), null) : null)
                .reporter(workItem.getReporterId() != null ?
                        new WorkItemDetailView.UserSummaryView(workItem.getReporterId(), null) : null)

                .createdAt(instantToEpochMilli(workItem.getCreatedAt()))
                .createdBy(workItem.getCreatedBy())
                .updatedAt(instantToEpochMilli(workItem.getUpdatedAt()))
                .updatedBy(workItem.getUpdatedBy())
                .build();
    }

    private static long instantToEpochMilli(Instant instant) {
        return instant != null ? instant.toEpochMilli() : 0L;
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
