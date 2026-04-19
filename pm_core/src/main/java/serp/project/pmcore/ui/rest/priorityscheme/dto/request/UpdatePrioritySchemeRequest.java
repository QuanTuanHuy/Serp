/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priorityscheme.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;

@Getter
@NoArgsConstructor
public class UpdatePrioritySchemeRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private Long defaultPriorityId;
    private boolean defaultPriorityIdProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setDefaultPriorityId(Long defaultPriorityId) {
        this.defaultPriorityId = defaultPriorityId;
        this.defaultPriorityIdProvided = true;
    }

    @JsonIgnore
    public PrioritySchemeUpdateData toData() {
        return new PrioritySchemeUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                defaultPriorityId,
                defaultPriorityIdProvided
        );
    }
}
