/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem;

import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;

public record WorkItemComponentView(
        Long id,
        Long tenantId,
        Long projectId,
        String name,
        String description,
        Long leadUserId,
        String assigneeType,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static WorkItemComponentView from(ProjectComponentEntity entity) {
        return new WorkItemComponentView(
                entity.getId(),
                entity.getTenantId(),
                entity.getProjectId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLeadUserId(),
                entity.getAssigneeType(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
