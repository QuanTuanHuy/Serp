/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetype.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateIssueTypeRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private String iconUrl;
    private boolean iconUrlProvided;
    private Integer hierarchyLevel;
    private boolean hierarchyLevelProvided;

    private String typeKey;
    private boolean typeKeyProvided;
    private Long tenantId;
    private boolean tenantIdProvided;
    private Boolean isSystem;
    private boolean isSystemProvided;

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

    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
        this.typeKeyProvided = true;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        this.tenantIdProvided = true;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        this.isSystemProvided = true;
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
