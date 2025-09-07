/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.GroupRoleModel;

@Repository
public interface IGroupRoleRepository extends IBaseRepository<GroupRoleModel> {
    List<GroupRoleModel> findByGroupId(Long groupId);
}
