/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.RoleEntity;

import java.util.List;

public interface IRolePort {
    RoleEntity save(RoleEntity role);
    RoleEntity getRoleByName(String name);
    List<RoleEntity> getAllRoles();
    List<RoleEntity> getRolesByIds(List<Long> roleIds);
    RoleEntity getRoleById(Long roleId);
}
