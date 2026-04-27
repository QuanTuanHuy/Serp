/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.projectcategory.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;

@Getter
@NoArgsConstructor
public class UpdateProjectCategoryRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    @JsonIgnore
    public ProjectCategoryUpdateData toData() {
        return new ProjectCategoryUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided
        );
    }
}
