/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflowscheme.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.application.workflowscheme.command.manageitems.ManageWorkflowSchemeItemsCommand;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageWorkflowSchemeItemsRequest {

    @NotEmpty(message = "items must not be empty")
    @Size(max = 100, message = "items must contain at most 100 items")
    private List<@Valid WorkflowSchemeItemRequest> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkflowSchemeItemRequest {

        @NotNull(message = "issueTypeId is required")
        @Min(value = 1, message = "issueTypeId must be greater than 0")
        private Long issueTypeId;

        @NotNull(message = "workflowId is required")
        @Min(value = 1, message = "workflowId must be greater than 0")
        private Long workflowId;

        public ManageWorkflowSchemeItemsCommand.WorkflowSchemeItemInput toInput() {
            return new ManageWorkflowSchemeItemsCommand.WorkflowSchemeItemInput(issueTypeId, workflowId);
        }
    }
}
