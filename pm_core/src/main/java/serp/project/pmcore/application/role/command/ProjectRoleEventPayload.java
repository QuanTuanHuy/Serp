/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.command;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;

public record ProjectRoleEventPayload(
        Long roleId,
        String name,
        boolean isSystem,
        Long performedBy,
        Long deletedAt
) {
    public static ProjectRoleEventPayload from(ProjectRoleEntity entity, Long performedBy) {
        return new ProjectRoleEventPayload(
                entity.getId(),
                entity.getName(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                performedBy,
                entity.getDeletedAt()
        );
    }
}
