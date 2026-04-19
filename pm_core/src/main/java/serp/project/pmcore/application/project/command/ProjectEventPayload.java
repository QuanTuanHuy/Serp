/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

public record ProjectEventPayload(
        Long projectId,
        String key,
        String name,
        Long leadUserId,
        Long categoryId,
        String projectTypeKey,
        Boolean archived,
        Long performedBy,
        Long occurredAt
) {
    public static ProjectEventPayload from(ProjectEntity entity, Long performedBy) {
        return new ProjectEventPayload(
                entity.getId(),
                entity.getKey(),
                entity.getName(),
                entity.getLeadUserId(),
                entity.getCategoryId(),
                entity.getProjectTypeKey(),
                entity.getIsArchived(),
                performedBy,
                System.currentTimeMillis()
        );
    }
}
