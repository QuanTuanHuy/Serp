/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.dto;

import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillSource;

public record UserSkillDraftData(
        Long skillId,
        SkillProficiency proficiency,
        Integer confidence,
        SkillSource source,
        Long verifiedAt
) {
}
