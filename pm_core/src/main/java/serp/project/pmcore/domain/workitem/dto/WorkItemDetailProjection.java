/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import java.time.Instant;

public interface WorkItemDetailProjection {
    Long getId();
    Long getProjectId();
    Long getIssueNo();
    String getKey();
    String getSummary();
    String getDescription();
    Long getResolutionId();
    Long getParentId();
    Long getSecurityLevelId();
    Instant getStartDate();
    Instant getDueDate();
    String getRank();
    Long getTimeOriginalEstimate();
    Long getTimeRemainingEstimate();
    Long getTimeSpent();
    Long getAssigneeId();
    Long getReporterId();
    Instant getCreatedAt();
    Long getCreatedBy();
    Instant getUpdatedAt();
    Long getUpdatedBy();

    Long getIssueTypeId();
    String getIssueTypeName();

    Long getPriorityId();
    String getPriorityName();
    String getPriorityColor();

    Long getStatusId();
    String getStatusName();

    Long getWorkflowStepId();
    String getWorkflowStepName();
}
