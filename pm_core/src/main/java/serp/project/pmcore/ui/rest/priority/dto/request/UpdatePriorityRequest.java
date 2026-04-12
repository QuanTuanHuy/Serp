/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priority.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePriorityRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private String iconUrl;
    private boolean iconUrlProvided;
    private String color;
    private boolean colorProvided;
    private Integer sequence;
    private boolean sequenceProvided;

    private String priorityKey;
    private boolean priorityKeyProvided;
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

    public void setColor(String color) {
        this.color = color;
        this.colorProvided = true;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
        this.sequenceProvided = true;
    }

    public void setPriorityKey(String priorityKey) {
        this.priorityKey = priorityKey;
        this.priorityKeyProvided = true;
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
    public PriorityUpdateData toData() {
        return new PriorityUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                iconUrl,
                iconUrlProvided,
                color,
                colorProvided,
                sequence,
                sequenceProvided
        );
    }
}
