/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.workflow.WorkflowView;

public record GetWorkflowByIdQuery(
        Long workflowId,
        Long tenantId
) implements IQuery<WorkflowView> {
}
