/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;

public record StatusCategoryView(
        Long id,
        Long tenantId,
        String name,
        String key,
        String color,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static StatusCategoryView from(StatusCategoryEntity entity) {
        return from(entity, Boolean.TRUE.equals(entity.getIsSystem()));
    }

    public static StatusCategoryView from(StatusCategoryEntity entity, boolean readOnly) {
        return new StatusCategoryView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getKey(),
                entity.getColor(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
