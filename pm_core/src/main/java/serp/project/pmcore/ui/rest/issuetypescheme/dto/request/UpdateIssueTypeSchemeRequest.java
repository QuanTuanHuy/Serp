/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetypescheme.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;

@Getter
@NoArgsConstructor
public class UpdateIssueTypeSchemeRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private Long defaultIssueTypeId;
    private boolean defaultIssueTypeIdProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setDefaultIssueTypeId(Long defaultIssueTypeId) {
        this.defaultIssueTypeId = defaultIssueTypeId;
        this.defaultIssueTypeIdProvided = true;
    }

    @JsonIgnore
    public IssueTypeSchemeUpdateData toData() {
        return new IssueTypeSchemeUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                defaultIssueTypeId,
                defaultIssueTypeIdProvided
        );
    }
}
