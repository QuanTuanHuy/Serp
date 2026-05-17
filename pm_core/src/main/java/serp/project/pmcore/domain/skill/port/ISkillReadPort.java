/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port;

import serp.project.pmcore.domain.skill.entity.SkillEntity;

import java.util.List;

public interface ISkillReadPort {
    List<SkillEntity> listActiveByIds(Long tenantId, List<Long> skillIds);
}
