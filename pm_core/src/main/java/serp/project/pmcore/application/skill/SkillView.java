/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.skill;

import lombok.Builder;
import lombok.Getter;
import serp.project.pmcore.domain.skill.entity.SkillEntity;

@Getter
@Builder
public class SkillView {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean active;

    public static SkillView from(SkillEntity entity) {
        return SkillView.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .build();
    }
}
