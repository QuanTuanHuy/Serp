/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillSource;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserSkillEntity extends BaseEntity {
    private Long tenantId;
    private Long userId;
    private Long skillId;
    private SkillProficiency proficiency;
    private Integer confidence;
    private SkillSource source;
    private Long verifiedAt;
    private Long deletedAt;
}
