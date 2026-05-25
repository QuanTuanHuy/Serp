/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionRulePort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WorkflowDraftVersionCloner {

    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IWorkflowTransitionRulePort workflowTransitionRulePort;

    public void cloneVersionTree(Long sourceWorkflowVersionId,
                                 Long targetWorkflowVersionId,
                                 Long tenantId,
                                 Long userId) {
        List<WorkflowStepEntity> sourceSteps = workflowStepPort
                .getWorkflowStepsByWorkflowVersionId(sourceWorkflowVersionId, tenantId);
        List<WorkflowTransitionEntity> sourceTransitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionId(sourceWorkflowVersionId, tenantId);

        Map<Long, Long> stepIdMap = cloneSteps(sourceSteps, targetWorkflowVersionId, tenantId, userId);
        Map<Long, Long> transitionIdMap = cloneTransitions(sourceTransitions, targetWorkflowVersionId, tenantId, userId, stepIdMap);
        cloneTransitionRules(sourceTransitions, tenantId, userId, transitionIdMap);
    }

    private Map<Long, Long> cloneSteps(List<WorkflowStepEntity> sourceSteps,
                                       Long targetWorkflowVersionId,
                                       Long tenantId,
                                       Long userId) {
        Map<Long, Long> stepIdMap = new HashMap<>();
        if (sourceSteps.isEmpty()) {
            return stepIdMap;
        }

        long now = System.currentTimeMillis();
        List<WorkflowStepEntity> draftSteps = new ArrayList<>();
        for (WorkflowStepEntity sourceStep : sourceSteps) {
            WorkflowStepEntity step = WorkflowStepEntity.builder()
                    .tenantId(tenantId)
                    .workflowVersionId(targetWorkflowVersionId)
                    .stepKey(sourceStep.getStepKey())
                    .name(sourceStep.getName())
                    .statusId(sourceStep.getStatusId())
                    .stepOrder(sourceStep.getStepOrder())
                    .isInitial(sourceStep.getIsInitial())
                    .isTerminal(sourceStep.getIsTerminal())
                    .deletedAt(null)
                    .build();
            step.applyCreate(userId, now);
            draftSteps.add(step);
        }

        List<WorkflowStepEntity> savedSteps = workflowStepPort.createWorkflowSteps(draftSteps);
        for (int index = 0; index < sourceSteps.size(); index++) {
            stepIdMap.put(sourceSteps.get(index).getId(), savedSteps.get(index).getId());
        }
        return stepIdMap;
    }

    private Map<Long, Long> cloneTransitions(List<WorkflowTransitionEntity> sourceTransitions,
                                             Long targetWorkflowVersionId,
                                             Long tenantId,
                                             Long userId,
                                             Map<Long, Long> stepIdMap) {
        Map<Long, Long> transitionIdMap = new HashMap<>();
        if (sourceTransitions.isEmpty()) {
            return transitionIdMap;
        }

        long now = System.currentTimeMillis();
        List<WorkflowTransitionEntity> draftTransitions = new ArrayList<>();
        for (WorkflowTransitionEntity sourceTransition : sourceTransitions) {
            WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                    .tenantId(tenantId)
                    .workflowVersionId(targetWorkflowVersionId)
                    .name(sourceTransition.getName())
                    .fromStepId(requireMappedId(stepIdMap, sourceTransition.getFromStepId()))
                    .toStepId(requireMappedId(stepIdMap, sourceTransition.getToStepId()))
                    .screenId(sourceTransition.getScreenId())
                    .sequence(sourceTransition.getSequence())
                    .deletedAt(null)
                    .build();
            transition.applyCreate(userId, now);
            draftTransitions.add(transition);
        }

        List<WorkflowTransitionEntity> savedTransitions = workflowTransitionPort.createWorkflowTransitions(draftTransitions);
        for (int index = 0; index < sourceTransitions.size(); index++) {
            transitionIdMap.put(sourceTransitions.get(index).getId(), savedTransitions.get(index).getId());
        }
        return transitionIdMap;
    }

    private void cloneTransitionRules(List<WorkflowTransitionEntity> sourceTransitions,
                                      Long tenantId,
                                      Long userId,
                                      Map<Long, Long> transitionIdMap) {
        if (sourceTransitions.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        List<WorkflowTransitionRuleEntity> draftRules = new ArrayList<>();
        for (WorkflowTransitionEntity sourceTransition : sourceTransitions) {
            Long targetTransitionId = transitionIdMap.get(sourceTransition.getId());
            if (targetTransitionId == null) {
                continue;
            }

            for (WorkflowTransitionRuleEntity sourceRule : workflowTransitionRulePort
                    .getWorkflowTransitionRulesByTransitionId(sourceTransition.getId(), tenantId)) {
                WorkflowTransitionRuleEntity rule = WorkflowTransitionRuleEntity.builder()
                        .tenantId(tenantId)
                        .transitionId(targetTransitionId)
                        .ruleStage(sourceRule.getRuleStage())
                        .ruleKey(sourceRule.getRuleKey())
                        .configJson(sourceRule.getConfigJson())
                        .sequence(sourceRule.getSequence())
                        .isEnabled(sourceRule.getIsEnabled())
                        .deletedAt(null)
                        .build();
                rule.applyCreate(userId, now);
                draftRules.add(rule);
            }
        }

        if (!draftRules.isEmpty()) {
            workflowTransitionRulePort.createWorkflowTransitionRules(draftRules);
        }
    }

    private Long requireMappedId(Map<Long, Long> idMapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        return idMapping.get(sourceId);
    }
}
