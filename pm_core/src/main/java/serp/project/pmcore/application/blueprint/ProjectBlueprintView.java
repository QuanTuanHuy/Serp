/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint;

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;

public record ProjectBlueprintView(
        Long id,
        Long tenantId,
        String name,
        String description,
        String projectTypeKey,
        String avatarUrl,
        Boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ProjectBlueprintView from(ProjectBlueprintEntity entity) {
        return new ProjectBlueprintView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTypeKey(),
                entity.getAvatarUrl(),
                entity.getIsSystem(),
                entity.isSystem(),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
