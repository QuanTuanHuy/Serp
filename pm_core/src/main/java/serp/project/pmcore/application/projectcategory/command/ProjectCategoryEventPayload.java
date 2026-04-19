/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory.command;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;

public record ProjectCategoryEventPayload(
        Long categoryId,
        String name,
        boolean isSystem,
        Long performedBy,
        Long deletedAt
) {
    public static ProjectCategoryEventPayload from(ProjectCategoryEntity entity, Long performedBy) {
        return new ProjectCategoryEventPayload(
                entity.getId(),
                entity.getName(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                performedBy,
                entity.getDeletedAt()
        );
    }
}
