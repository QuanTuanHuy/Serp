/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.dto;

public record WorkflowSchemeUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        Long defaultWorkflowId,
        boolean defaultWorkflowIdProvided
) {
}
