/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;

public record OptimizationSkillRequirement(
        Long workItemId,
        Long skillId,
        SkillRequirementType requirementType,
        SkillProficiency minProficiency,
        Integer weight
) {
}
