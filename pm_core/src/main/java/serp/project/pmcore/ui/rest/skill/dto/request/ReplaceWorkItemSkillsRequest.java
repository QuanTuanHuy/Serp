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
import serp.project.pmcore.domain.skill.dto.WorkItemSkillDraftData;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.enums.SkillSource;

import java.util.List;

@Getter
@Setter
public class ReplaceWorkItemSkillsRequest {
    @Valid
    private List<Item> items = List.of();

    public List<WorkItemSkillDraftData> toData() {
        return items.stream()
                .map(item -> new WorkItemSkillDraftData(
                        item.skillId,
                        item.requirementType,
                        item.minProficiency,
                        item.weight,
                        item.source == null ? SkillSource.MANUAL : item.source
                ))
                .toList();
    }

    @Getter
    @Setter
    public static class Item {
        @NotNull
        private Long skillId;

        @NotNull
        private SkillRequirementType requirementType;

        @NotNull
        private SkillProficiency minProficiency;

        @NotNull
        @Min(1)
        @Max(100)
        private Integer weight;

        private SkillSource source;
    }
}
