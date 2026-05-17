/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.SkillModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISkillRepository extends JpaRepository<SkillModel, Long> {
    List<SkillModel> findAllByTenantIdAndIdInAndActiveTrue(Long tenantId, List<Long> ids);

    List<SkillModel> findAllByTenantIdAndActiveTrueOrderByCodeAsc(Long tenantId);

    Optional<SkillModel> findByTenantIdAndIdAndActiveTrue(Long tenantId, Long id);

    Optional<SkillModel> findByTenantIdAndCodeIgnoreCaseAndActiveTrue(Long tenantId, String code);
}
