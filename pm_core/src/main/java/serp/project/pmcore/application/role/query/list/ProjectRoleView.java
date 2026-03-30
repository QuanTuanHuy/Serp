/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.list;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;

public record ProjectRoleView(
        Long tenantId,
        String name,
        String description,
        Boolean isDefault
) {
    public static ProjectRoleView from(ProjectRoleEntity entity) {
        return new ProjectRoleView(
                entity.getTenantId(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsSystem()
        );
    }
}
