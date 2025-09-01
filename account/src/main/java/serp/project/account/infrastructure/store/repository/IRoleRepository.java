/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.RoleModel;

@Repository
public interface IRoleRepository extends IBaseRepository<RoleModel> {
    Optional<RoleModel> findByName(String name);

    List<RoleModel> findByIdIn(List<Long> ids);
}
