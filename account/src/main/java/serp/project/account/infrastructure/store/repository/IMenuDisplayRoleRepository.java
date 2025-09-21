/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.MenuDisplayRoleModel;

@Repository
public interface IMenuDisplayRoleRepository extends IBaseRepository<MenuDisplayRoleModel> {
    List<MenuDisplayRoleModel> findByRoleIdIn(List<Long> roleIds);

    void deleteByIdIn(List<Long> ids);
}
