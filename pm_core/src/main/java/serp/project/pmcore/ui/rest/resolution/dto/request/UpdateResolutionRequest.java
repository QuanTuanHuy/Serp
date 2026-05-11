/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resolution.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;

@Getter
@NoArgsConstructor
public class UpdateResolutionRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
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

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
        this.sequenceProvided = true;
    }

    @JsonIgnore
    public ResolutionUpdateData toData() {
        return new ResolutionUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                sequence,
                sequenceProvided
        );
    }
}
