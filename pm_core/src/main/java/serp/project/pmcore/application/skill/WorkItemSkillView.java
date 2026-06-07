/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill;

import lombok.Builder;
import lombok.Getter;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.enums.SkillSource;

@Getter
@Builder
public class WorkItemSkillView {
    private Long id;
    private Long skillId;
    private SkillRequirementType requirementType;
    private SkillProficiency minProficiency;
    private Integer weight;
    private SkillSource source;

    public static WorkItemSkillView from(WorkItemSkillEntity entity) {
        return WorkItemSkillView.builder()
                .id(entity.getId())
                .skillId(entity.getSkillId())
                .requirementType(entity.getRequirementType())
                .minProficiency(entity.getMinProficiency())
                .weight(entity.getWeight())
                .source(entity.getSource())
                .build();
    }
}
