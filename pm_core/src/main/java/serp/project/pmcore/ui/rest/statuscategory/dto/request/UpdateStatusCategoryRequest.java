/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.statuscategory.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;

@Getter
@NoArgsConstructor
public class UpdateStatusCategoryRequest {

    private String name;
    private boolean nameProvided;
    private String key;
    private boolean keyProvided;
    private String color;
    private boolean colorProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setKey(String key) {
        this.key = key;
        this.keyProvided = true;
    }

    public void setColor(String color) {
        this.color = color;
        this.colorProvided = true;
    }

    @JsonIgnore
    public StatusCategoryUpdateData toData() {
        return new StatusCategoryUpdateData(
                name,
                nameProvided,
                key,
                keyProvided,
                color,
                colorProvided
        );
    }
}
