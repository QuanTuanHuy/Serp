/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.blueprint.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;

@Getter
@NoArgsConstructor
public class UpdateProjectBlueprintRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private String avatarUrl;
    private boolean avatarUrlProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        this.avatarUrlProvided = true;
    }

    @JsonIgnore
    public ProjectBlueprintUpdateData toData() {
        return new ProjectBlueprintUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                avatarUrl,
                avatarUrlProvided
        );
    }
}
