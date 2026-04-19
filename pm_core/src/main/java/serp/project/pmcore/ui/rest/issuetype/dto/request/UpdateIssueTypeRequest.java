/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetype.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;

@Getter
@NoArgsConstructor
public class UpdateIssueTypeRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private String iconUrl;
    private boolean iconUrlProvided;
    private Integer hierarchyLevel;
    private boolean hierarchyLevelProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        this.iconUrlProvided = true;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
        this.hierarchyLevelProvided = true;
    }

    @JsonIgnore
    public IssueTypeUpdateData toData() {
        return new IssueTypeUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                iconUrl,
                iconUrlProvided,
                hierarchyLevel,
                hierarchyLevelProvided
        );
    }
}
