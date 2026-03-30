/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemResult;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkItemResponse {
    private Long id;
    private Long projectId;
    private Long issueTypeId;

    private Long issueNo;
    private String key;
    private String summary;
    private String description;

    private Long workflowStepId;
    private Long statusId;
    private Long priorityId;
    private Long resolutionId;
    private Long assigneeId;
    private Long reporterId;
    private Long parentId;
    private Long securityLevelId;

    private Long dueDate;
    private String rank;

    private Long timeOriginalEstimate;
    private Long timeRemainingEstimate;
    private Long timeSpent;

    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;

    public static WorkItemResponse from(CreateWorkItemResult result) {
        return WorkItemResponse.builder()
                .id(result.getId())
                .projectId(result.getProjectId())
                .issueTypeId(result.getIssueTypeId())
                .issueNo(result.getIssueNo())
                .key(result.getKey())
                .summary(result.getSummary())
                .description(result.getDescription())
                .workflowStepId(result.getWorkflowStepId())
                .statusId(result.getStatusId())
                .priorityId(result.getPriorityId())
                .resolutionId(result.getResolutionId())
                .assigneeId(result.getAssigneeId())
                .reporterId(result.getReporterId())
                .parentId(result.getParentId())
                .securityLevelId(result.getSecurityLevelId())
                .dueDate(result.getDueDate())
                .rank(result.getRank())
                .timeOriginalEstimate(result.getTimeOriginalEstimate())
                .timeRemainingEstimate(result.getTimeRemainingEstimate())
                .timeSpent(result.getTimeSpent())
                .createdAt(result.getCreatedAt())
                .createdBy(result.getCreatedBy())
                .updatedAt(result.getUpdatedAt())
                .updatedBy(result.getUpdatedBy())
                .build();
    }
}
