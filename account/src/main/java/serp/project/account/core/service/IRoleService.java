/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;

import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;

public interface IRoleService {
    RoleEntity createRole(CreateRoleDto request);

    RoleEntity getRoleByName(String name);

    RoleEntity getOrCreateOrganizationRole(String roleName);

    List<RoleEntity> getAllRoles();

    List<RoleEntity> getRolesByGroupId(Long groupId);

    List<RoleEntity> getRolesByScope(RoleScope scope);

    List<RoleEntity> getRolesByScopeAndTypeList(RoleScope scope, List<RoleType> types);

    void addPermissionsToRole(Long roleId, List<Long> permissionIds);

    RoleEntity getHighestRole(List<RoleEntity> roles);
}
