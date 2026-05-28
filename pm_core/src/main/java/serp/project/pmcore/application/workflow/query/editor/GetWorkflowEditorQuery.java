/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.editor;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetWorkflowEditorQuery(
        Long workflowId,
        Long tenantId
) implements IQuery<WorkflowEditorView> {
}
