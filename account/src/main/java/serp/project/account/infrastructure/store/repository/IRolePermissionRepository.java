/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.RolePermissionModel;

@Repository
public interface IRolePermissionRepository extends IBaseRepository<RolePermissionModel> {
    List<RolePermissionModel> findByRoleId(Long roleId);
}
