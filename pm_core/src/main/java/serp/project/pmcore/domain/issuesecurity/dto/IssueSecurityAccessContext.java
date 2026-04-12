/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuesecurity.dto;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public record IssueSecurityAccessContext(
        Long projectId,
        Long tenantId,
        Long leadUserId,
        Long issueSecuritySchemeId,
        Long workItemId,
        Long securityLevelId,
        Long reporterUserId,
        Long assigneeUserId
) {
    public static IssueSecurityAccessContext from(ProjectEntity project, WorkItemEntity workItem) {
        return new IssueSecurityAccessContext(
                project.getId(),
                project.getTenantId(),
                project.getLeadUserId(),
                project.getIssueSecuritySchemeId(),
                workItem.getId(),
                workItem.getSecurityLevelId(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
    }
}
