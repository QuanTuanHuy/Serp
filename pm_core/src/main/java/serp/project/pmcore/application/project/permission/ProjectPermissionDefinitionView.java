/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission;

import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;

public record ProjectPermissionDefinitionView(
        String permissionKey,
        String name,
        String description,
        String category
) {
    public static ProjectPermissionDefinitionView from(PermissionDefinitionEntity entity) {
        return new ProjectPermissionDefinitionView(
                entity.getPermissionKey(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory()
        );
    }
}
