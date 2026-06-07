/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflowscheme.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;

@Getter
@NoArgsConstructor
public class UpdateWorkflowSchemeRequest {

    private String name;
    private boolean nameProvided;
    private String description;
    private boolean descriptionProvided;
    private Long defaultWorkflowId;
    private boolean defaultWorkflowIdProvided;

    public void setName(String name) {
        this.name = name;
        this.nameProvided = true;
    }

    public void setDescription(String description) {
        this.description = description;
        this.descriptionProvided = true;
    }

    public void setDefaultWorkflowId(Long defaultWorkflowId) {
        this.defaultWorkflowId = defaultWorkflowId;
        this.defaultWorkflowIdProvided = true;
    }

    @JsonIgnore
    public WorkflowSchemeUpdateData toData() {
        return new WorkflowSchemeUpdateData(
                name,
                nameProvided,
                description,
                descriptionProvided,
                defaultWorkflowId,
                defaultWorkflowIdProvided
        );
    }
}
