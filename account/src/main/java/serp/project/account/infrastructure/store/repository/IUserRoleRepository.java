/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.UserRoleModel;

@Repository
public interface IUserRoleRepository extends IBaseRepository<UserRoleModel> {
    List<UserRoleModel> findByUserId(Long userId);

    List<UserRoleModel> findByUserIdIn(List<Long> userIds);
}
