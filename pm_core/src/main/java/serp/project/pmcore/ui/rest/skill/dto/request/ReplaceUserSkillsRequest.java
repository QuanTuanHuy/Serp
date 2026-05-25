/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.skill.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import serp.project.pmcore.domain.skill.dto.UserSkillDraftData;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillSource;

import java.util.List;

@Getter
@Setter
public class ReplaceUserSkillsRequest {
    @Valid
    private List<Item> items = List.of();

    public List<UserSkillDraftData> toData() {
        return items.stream()
                .map(item -> new UserSkillDraftData(
                        item.skillId,
                        item.proficiency,
                        item.confidence,
                        item.source == null ? SkillSource.MANUAL : item.source,
                        item.verifiedAt
                ))
                .toList();
    }

    @Getter
    @Setter
    public static class Item {
        @NotNull
        private Long skillId;

        @NotNull
        private SkillProficiency proficiency;

        @Min(0)
        @Max(100)
        private Integer confidence;

        private SkillSource source;

        private Long verifiedAt;
    }
}
