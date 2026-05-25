/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.model;

import serp.project.pmcore.domain.skill.enums.SkillProficiency;

public record OptimizationCandidateSkill(
        Long candidateId,
        Long skillId,
        SkillProficiency proficiency,
        Integer confidence
) {
}
