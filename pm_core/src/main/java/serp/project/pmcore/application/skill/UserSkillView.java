/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill;

import lombok.Builder;
import lombok.Getter;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillSource;

@Getter
@Builder
public class UserSkillView {
    private Long id;
    private Long skillId;
    private SkillProficiency proficiency;
    private Integer confidence;
    private SkillSource source;
    private Long verifiedAt;

    public static UserSkillView from(UserSkillEntity entity) {
        return UserSkillView.builder()
                .id(entity.getId())
                .skillId(entity.getSkillId())
                .proficiency(entity.getProficiency())
                .confidence(entity.getConfidence())
                .source(entity.getSource())
                .verifiedAt(entity.getVerifiedAt())
                .build();
    }
}
