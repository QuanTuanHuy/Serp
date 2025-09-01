/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.PermissionModel;

@Repository
public interface IPermissionRepository extends IBaseRepository<PermissionModel> {
    Optional<PermissionModel> findByName(String name);
    List<PermissionModel> findByIdIn(List<Long> ids);
}
