/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;

public record WorkflowSchemeItemView(
        Long id,
        Long issueTypeId,
        Long workflowId,
        WorkflowSchemeIssueTypeView issueType,
        WorkflowSchemeWorkflowView workflow
) {
    public static WorkflowSchemeItemView from(WorkflowSchemeItemEntity entity,
                                              WorkflowSchemeIssueTypeView issueType,
                                              WorkflowSchemeWorkflowView workflow) {
        return new WorkflowSchemeItemView(
                entity.getId(),
                entity.getIssueTypeId(),
                entity.getWorkflowId(),
                issueType,
                workflow
        );
    }
}
