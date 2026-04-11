/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.Map;

public interface IWorkItemTransitionRuleEvaluator {
    void evaluateConditions(ResolvedTransitionExecution execution,
                            WorkItemEntity workItem,
                            ProjectEntity project,
                            Long userId);

    Long evaluateValidatorsAndResolveResolution(ResolvedTransitionExecution execution,
                                                WorkItemEntity workItem,
                                                TransitionWorkItemStatusData data,
                                                Long requestedResolutionId,
                                                Map<String, Object> existingCustomFieldValues,
                                                Long tenantId);

    void applyPostFunctions(ResolvedTransitionExecution execution,
                            WorkItemEntity candidate,
                            Long requestedResolutionId);
}
