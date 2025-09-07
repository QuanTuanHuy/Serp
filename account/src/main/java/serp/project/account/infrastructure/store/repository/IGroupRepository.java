/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.GroupModel;

@Repository
public interface IGroupRepository extends IBaseRepository<GroupModel> {
    Optional<GroupModel> findByGroupName(String groupName);
    boolean existsByGroupName(String groupName);
}
