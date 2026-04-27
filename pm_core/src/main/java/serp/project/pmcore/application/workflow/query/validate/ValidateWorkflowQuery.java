/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.validate;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.workflow.WorkflowValidationView;

public record ValidateWorkflowQuery(
        Long workflowId,
        Long tenantId
) implements IQuery<WorkflowValidationView> {
}
