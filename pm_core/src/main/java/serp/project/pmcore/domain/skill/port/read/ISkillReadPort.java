/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.read;

import serp.project.pmcore.domain.skill.entity.SkillEntity;

import java.util.List;
import java.util.Optional;

public interface ISkillReadPort {
    Optional<SkillEntity> findActiveById(Long tenantId, Long skillId);

    Optional<SkillEntity> findActiveByCode(Long tenantId, String code);

    List<SkillEntity> listActive(Long tenantId);

    List<SkillEntity> listActiveByIds(Long tenantId, List<Long> skillIds);
}
