/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;

@Getter
@NoArgsConstructor
public class UpdateProjectRequest {

    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    private boolean nameProvided;

    @Pattern(regexp = "^[A-Z][A-Z0-9]{1,9}$",
            message = "Project key must be 2-10 uppercase alphanumeric characters starting with a letter")
    private String key;
    private boolean keyProvided;

    @Size(max = 10000, message = "Description must be at most 10000 characters")
    private String description;
    private boolean descriptionProvided;

    private Long leadUserId;
    private boolean leadUserIdProvided;

    private Long categoryId;
    private boolean categoryIdProvided;

    private String url;
    private boolean urlProvided;

    private Long avatarId;
    private boolean avatarIdProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setKey(String key) {
        this.key = key;
        this.keyProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setLeadUserId(Long leadUserId) {
        this.leadUserId = leadUserId;
        this.leadUserIdProvided = true;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        this.categoryIdProvided = true;
    }

    public void setUrl(String url) {
        this.url = url;
        this.urlProvided = true;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
        this.avatarIdProvided = true;
    }

    @JsonIgnore
    public ProjectUpdateData toData() {
        return new ProjectUpdateData(
                name,
                nameProvided,
                key,
                keyProvided,
                description,
                descriptionProvided,
                leadUserId,
                leadUserIdProvided,
                categoryId,
                categoryIdProvided,
                url,
                urlProvided,
                avatarId,
                avatarIdProvided
        );
    }
}
