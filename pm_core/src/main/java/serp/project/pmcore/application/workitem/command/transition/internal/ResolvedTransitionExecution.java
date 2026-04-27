/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.internal;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;

import java.util.List;

public record ResolvedTransitionExecution(
        IssueTypeEntity issueType,
        WorkflowVersionEntity workflowVersion,
        WorkflowStepEntity currentStep,
        WorkflowStepEntity targetStep,
        WorkflowTransitionEntity transition,
        List<WorkflowTransitionRuleEntity> rules,
        StatusEntity targetStatus,
        StatusCategoryEntity targetStatusCategory
) {
}
