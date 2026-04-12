/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priority.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;

@Getter
@NoArgsConstructor
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
