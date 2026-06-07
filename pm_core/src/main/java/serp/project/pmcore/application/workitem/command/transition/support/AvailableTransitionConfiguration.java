/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.support;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public record AvailableTransitionConfiguration(
        WorkflowTransitionEntity transition,
        WorkflowStepEntity currentStep,
        WorkflowStepEntity targetStep,
        StatusEntity targetStatus,
        StatusCategoryEntity targetStatusCategory
) {
}
