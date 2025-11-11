/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;

import java.util.List;

public interface IMenuDisplayRolePort {
    void save(List<MenuDisplayRoleEntity> menuDisplayRoles);

    List<MenuDisplayRoleEntity> getByRoleIds(List<Long> roleIds);

    List<MenuDisplayRoleEntity> getByMenuDisplayIds(List<Long> menuDisplayIds);

    List<MenuDisplayRoleEntity> getByRoleIdAndMenuDisplayIds(Long roleId, List<Long> menuDisplayIds);

    void deleteByRoleIdAndMenuDisplayIds(Long roleId, List<Long> menuDisplayIds);

    void deleteByIds(List<Long> ids);
}
