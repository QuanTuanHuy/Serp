/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.read;

import serp.project.pmcore.domain.skill.entity.UserSkillEntity;

import java.util.List;

public interface IUserSkillReadPort {
    List<UserSkillEntity> listActive(Long tenantId, Long userId);

    List<UserSkillEntity> listActiveByUserIds(Long tenantId, List<Long> userIds);
}
