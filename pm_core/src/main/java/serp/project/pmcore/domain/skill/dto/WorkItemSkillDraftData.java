/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.dto;

import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.enums.SkillSource;

public record WorkItemSkillDraftData(
        Long skillId,
        SkillRequirementType requirementType,
        SkillProficiency minProficiency,
        Integer weight,
        SkillSource source
) {
}
