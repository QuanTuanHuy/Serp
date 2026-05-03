/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeDetailView;

public record GetWorkflowSchemeByIdQuery(
        Long schemeId,
        Long tenantId
) implements IQuery<WorkflowSchemeDetailView> {
}
