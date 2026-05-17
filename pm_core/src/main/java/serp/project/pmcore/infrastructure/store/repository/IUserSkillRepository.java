/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.UserSkillModel;

import java.util.List;

@Repository
public interface IUserSkillRepository extends JpaRepository<UserSkillModel, Long> {
    List<UserSkillModel> findAllByTenantIdAndUserIdIn(Long tenantId, List<Long> userIds);

    List<UserSkillModel> findAllByTenantIdAndUserId(Long tenantId, Long userId);
}
