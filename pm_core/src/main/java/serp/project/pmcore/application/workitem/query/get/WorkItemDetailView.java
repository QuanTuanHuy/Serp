/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import serp.project.pmcore.application.shared.dto.user.UserSummary;
import serp.project.pmcore.application.workitem.WorkItemComponentView;
import serp.project.pmcore.domain.workitem.dto.WorkItemDetailProjection;

import java.time.Instant;
import java.util.List;

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

        ScheduleSummaryView schedule,
        IssueTypeSummaryView issueType,
        UserSummary assignee,
        UserSummary reporter,
        WorkflowStepSummaryView workflowStep,
        StatusSummaryView status,
        PrioritySummaryView priority,
        ParentSummaryView parent,
        List<WorkItemComponentView> components,
        SubtaskStatsView subtaskStats,
        LinkStatsView linkStats,
        CommentStatsView commentStats,

        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {

    public static WorkItemDetailView from(WorkItemDetailProjection workItem,
                                          UserSummary assignee,
                                          UserSummary reporter,
                                          ScheduleSummaryView schedule,
                                          List<WorkItemComponentView> components,
                                          SubtaskStatsView subtaskStats,
                                          LinkStatsView linkStats,
                                          CommentStatsView commentStats) {

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
                .schedule(schedule)

                .issueType(workItem.getIssueTypeId() != null ?
                        new WorkItemDetailView.IssueTypeSummaryView(
                                workItem.getIssueTypeId(),
                                workItem.getIssueTypeName(),
                                workItem.getIssueTypeIconUrl(),
                                workItem.getIssueTypeHierarchyLevel()
                        ) : null)
                .priority(workItem.getPriorityId() != null ?
                        new WorkItemDetailView.PrioritySummaryView(workItem.getPriorityId(), workItem.getPriorityName(), workItem.getPriorityColor()) : null)
                .status(workItem.getStatusId() != null ?
                        new WorkItemDetailView.StatusSummaryView(
                                workItem.getStatusId(),
                                workItem.getStatusName(),
                                workItem.getStatusKey()
                        ) : null)
                .workflowStep(workItem.getWorkflowStepId() != null ?
                        new WorkItemDetailView.WorkflowStepSummaryView(workItem.getWorkflowStepId(), workItem.getWorkflowStepName()) : null)
                .assignee(assignee)
                .reporter(reporter)
                .parent(workItem.getParentId() != null ?
                        new WorkItemDetailView.ParentSummaryView(
                                workItem.getParentId(),
                                workItem.getParentKey(),
                                workItem.getParentSummary()
                        ) : null)
                .components(components == null || components.isEmpty() ? null : components)
                .subtaskStats(subtaskStats)
                .linkStats(linkStats)
                .commentStats(commentStats)

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
            String name,
            String iconUrl,
            Integer hierarchyLevel
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
            String name,
            String key
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PrioritySummaryView(
            Long id,
            String name,
            String color
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ParentSummaryView(
            Long id,
            String key,
            String summary
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SubtaskStatsView(
            long total,
            long done
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LinkStatsView(
            long total
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CommentStatsView(
            long total
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ScheduleSummaryView(
            Long plannedStart,
            Long plannedEnd,
            String source,
            Boolean locked,
            Long sourceRunId
    ) {
    }
}
