/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.write;

import serp.project.pmcore.domain.skill.entity.UserSkillEntity;

import java.util.List;

public interface IUserSkillWritePort {
    void softDeleteActive(Long tenantId, Long userId, Long updatedBy, Long now);

    List<UserSkillEntity> saveAll(List<UserSkillEntity> skills);
}
