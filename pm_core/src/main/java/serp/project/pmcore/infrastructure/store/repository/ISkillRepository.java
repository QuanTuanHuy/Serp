/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.SkillModel;

import java.util.List;

@Repository
public interface ISkillRepository extends JpaRepository<SkillModel, Long> {
    List<SkillModel> findAllByTenantIdAndIdInAndActiveTrue(Long tenantId, List<Long> ids);
}
