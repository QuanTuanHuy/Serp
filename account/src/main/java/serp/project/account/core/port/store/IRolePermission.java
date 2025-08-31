/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import serp.project.account.core.domain.entity.RolePermissionEntity;

public interface IRolePermission {
    List<RolePermissionEntity> saveAll(List<RolePermissionEntity> rolePermissions);
    List<RolePermissionEntity> getRolePermissionsByRoleId(Long roleId);
}
