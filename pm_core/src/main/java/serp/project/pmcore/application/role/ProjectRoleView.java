/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;

public record ProjectRoleView(
        Long id,
        Long tenantId,
        String name,
        String description,
        Boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static ProjectRoleView from(ProjectRoleEntity entity) {
        return from(entity, Boolean.TRUE.equals(entity.getIsSystem()));
    }

    public static ProjectRoleView from(ProjectRoleEntity entity, boolean readOnly) {
        return new ProjectRoleView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsSystem(),
                readOnly,
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
