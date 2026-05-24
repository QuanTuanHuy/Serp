/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component;

import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;

public record ProjectComponentView(
        Long id,
        Long tenantId,
        Long projectId,
        String name,
        String description,
        Long leadUserId,
        String assigneeType,
        Long issueCount,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ProjectComponentView from(ProjectComponentEntity entity) {
        return new ProjectComponentView(
                entity.getId(),
                entity.getTenantId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLeadUserId(),
                entity.getAssigneeType(),
                entity.getIssueCount() == null ? 0L : entity.getIssueCount(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
