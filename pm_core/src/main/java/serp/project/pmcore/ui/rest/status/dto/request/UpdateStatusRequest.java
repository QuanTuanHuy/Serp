/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.status.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;

@Getter
@NoArgsConstructor
public class UpdateStatusRequest {

    private String statusKey;
    private boolean statusKeyProvided;
    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private String iconUrl;
    private boolean iconUrlProvided;
    private Long statusCategoryId;
    private boolean statusCategoryIdProvided;

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
        this.statusKeyProvided = true;
    }

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

    public void setStatusCategoryId(Long statusCategoryId) {
        this.statusCategoryId = statusCategoryId;
        this.statusCategoryIdProvided = true;
    }

    @JsonIgnore
    public StatusUpdateData toData() {
        return new StatusUpdateData(
                statusKey,
                statusKeyProvided,
                name,
                nameProvided,
                description,
                descriptionProvided,
                iconUrl,
                iconUrlProvided,
                statusCategoryId,
                statusCategoryIdProvided
        );
    }
}
