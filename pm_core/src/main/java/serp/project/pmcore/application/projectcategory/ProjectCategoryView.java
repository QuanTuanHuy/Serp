/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;

public record ProjectCategoryView(
        Long id,
        Long tenantId,
        String name,
        String description,
        boolean isSystem,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ProjectCategoryView from(ProjectCategoryEntity entity) {
        return new ProjectCategoryView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
