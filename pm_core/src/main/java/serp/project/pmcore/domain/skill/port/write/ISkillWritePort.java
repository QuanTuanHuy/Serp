/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.port.write;

import serp.project.pmcore.domain.skill.entity.SkillEntity;

public interface ISkillWritePort {
    SkillEntity save(SkillEntity skill);
}
