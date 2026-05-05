/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.component.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;

@Getter
@NoArgsConstructor
public class UpdateProjectComponentRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private Long leadUserId;
    private boolean leadUserIdProvided;
    private String assigneeType;
    private boolean assigneeTypeProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setLeadUserId(Long leadUserId) {
        this.leadUserId = leadUserId;
        this.leadUserIdProvided = true;
    }

    public void setAssigneeType(String assigneeType) {
        this.assigneeType = assigneeType;
        this.assigneeTypeProvided = true;
    }

    @JsonIgnore
    public ProjectComponentUpdateData toData() {
        return new ProjectComponentUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                leadUserId,
                leadUserIdProvided,
                assigneeType,
                assigneeTypeProvided
        );
    }
}
