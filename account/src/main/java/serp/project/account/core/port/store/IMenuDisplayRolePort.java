package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;

import java.util.List;

public interface IMenuDisplayRolePort {
    void save(List<MenuDisplayRoleEntity> menuDisplayRoles);
    List<MenuDisplayRoleEntity> getByRoleIds(List<Long> roleIds);
}
