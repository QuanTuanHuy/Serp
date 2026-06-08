/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;

public record ProjectPermissionSchemeView(
        Long id,
        String name,
        String description,
        boolean tenantOwned
) {
    public static ProjectPermissionSchemeView from(PermissionSchemeEntity entity, Long tenantId) {
        return new ProjectPermissionSchemeView(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getTenantId() != null && entity.getTenantId().equals(tenantId)
        );
    }
}
