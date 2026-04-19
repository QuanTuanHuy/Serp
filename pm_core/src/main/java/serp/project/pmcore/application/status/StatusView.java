/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status;

import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public record StatusView(
        Long id,
        Long tenantId,
        String statusKey,
        String name,
        String description,
        String iconUrl,
        Long statusCategoryId,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static StatusView from(StatusEntity entity) {
        return from(entity, Boolean.TRUE.equals(entity.getIsSystem()));
    }

    public static StatusView from(StatusEntity entity, boolean readOnly) {
        return new StatusView(
                entity.getId(),
                entity.getTenantId(),
                entity.getStatusKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getIconUrl(),
                entity.getCategoryId(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
