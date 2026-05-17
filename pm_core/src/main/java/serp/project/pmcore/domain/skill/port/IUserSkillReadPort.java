/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port;

import serp.project.pmcore.domain.skill.entity.UserSkillEntity;

import java.util.List;

public interface IUserSkillReadPort {
    List<UserSkillEntity> listActiveByUserIds(Long tenantId, List<Long> userIds);
}
