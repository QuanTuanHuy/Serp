/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.UserGroupModel;

@Repository
public interface IUserGroupRepository extends IBaseRepository<UserGroupModel> {
    List<UserGroupModel> findByUserId(Long userId);
}
