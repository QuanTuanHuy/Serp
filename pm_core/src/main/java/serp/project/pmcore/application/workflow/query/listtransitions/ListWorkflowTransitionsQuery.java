/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.listtransitions;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;

import java.util.List;

public record ListWorkflowTransitionsQuery(
        Long workflowId,
        Long fromStepId,
        Long tenantId
) implements IQuery<List<WorkflowTransitionView>> {
}
