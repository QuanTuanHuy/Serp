package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.exception.*;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
import serp.project.pmcore.domain.port.store.IWorkflowTransitionPort;
import serp.project.pmcore.domain.port.store.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.materializer.StatusMaterializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkflowVersionTreeCloner {

    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IWorkflowTransitionRulePort workflowTransitionRulePort;
    private final StatusMaterializer statusMaterializer;
    private final ScreenCloner screenCloner;

    public void cloneVersionTree(WorkflowVersionEntity sourceVersion,
                                 Long targetWorkflowVersionId,
                                 Long tenantId,
                                 Long userId,
                                 CloneMode cloneMode,
                                 ProvisioningExecutionContext context) {
        validateRequired(sourceVersion, "sourceVersion");
        validateRequired(targetWorkflowVersionId, "targetWorkflowVersionId");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<WorkflowStepEntity> sourceSteps = workflowStepPort
                .getWorkflowStepsByWorkflowVersionIdIncludingSystem(sourceVersion.getId(), tenantId);

        List<WorkflowTransitionEntity> sourceTransitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(sourceVersion.getId(), tenantId);

        Map<Long, Long> statusIdMap = materializeStatuses(sourceSteps, tenantId, userId);
        Map<Long, Long> stepIdMap = cloneSteps(
                sourceSteps,
                targetWorkflowVersionId,
                tenantId,
                userId,
                statusIdMap
        );
        Map<Long, Long> screenIdMap = cloneTransitionScreens(
                sourceTransitions,
                tenantId,
                userId,
                cloneMode,
                context
        );
        Map<Long, Long> transitionIdMap = cloneTransitions(
                sourceTransitions,
                targetWorkflowVersionId,
                tenantId,
                userId,
                stepIdMap,
                screenIdMap
        );

        cloneTransitionRules(
                sourceTransitions,
                tenantId,
                userId,
                transitionIdMap
        );
    }

    private Map<Long, Long> materializeStatuses(List<WorkflowStepEntity> sourceSteps,
                                                Long tenantId,
                                                Long userId) {
        Map<Long, Long> statusIdMap = new HashMap<>();
        Set<Long> sourceStatusIds = new HashSet<>();

        for (WorkflowStepEntity step : sourceSteps) {
            if (step.getStatusId() != null) {
                sourceStatusIds.add(step.getStatusId());
            }
        }

        for (Long sourceStatusId : sourceStatusIds) {
            statusIdMap.put(
                    sourceStatusId,
                    statusMaterializer.materialize(sourceStatusId, tenantId, userId)
            );
        }

        return statusIdMap;
    }

    private Map<Long, Long> cloneSteps(List<WorkflowStepEntity> sourceSteps,
                                       Long targetWorkflowVersionId,
                                       Long tenantId,
                                       Long userId,
                                       Map<Long, Long> statusIdMap) {
        Map<Long, Long> stepIdMap = new HashMap<>();

        if (sourceSteps.isEmpty()) {
            return stepIdMap;
        }

        long now = System.currentTimeMillis();
        List<WorkflowStepEntity> clonedSteps = new ArrayList<>();
        for (WorkflowStepEntity step : sourceSteps) {
            clonedSteps.add(WorkflowStepEntity.builder()
                    .tenantId(tenantId)
                    .workflowVersionId(targetWorkflowVersionId)
                    .stepKey(step.getStepKey())
                    .name(step.getName())
                    .statusId(requireMappedId(statusIdMap, step.getStatusId(), "status"))
                    .stepOrder(step.getStepOrder())
                    .isInitial(step.getIsInitial())
                    .isTerminal(step.getIsTerminal())
                    .createdAt(now)
                    .createdBy(userId)
                    .build());
        }

        List<WorkflowStepEntity> savedSteps = workflowStepPort.createWorkflowSteps(clonedSteps);
        for (int i = 0; i < sourceSteps.size(); i++) {
            stepIdMap.put(sourceSteps.get(i).getId(), savedSteps.get(i).getId());
        }

        return stepIdMap;
    }

    private Map<Long, Long> cloneTransitionScreens(List<WorkflowTransitionEntity> sourceTransitions,
                                                    Long tenantId,
                                                    Long userId,
                                                    CloneMode cloneMode,
                                                    ProvisioningExecutionContext context) {
        Map<Long, Long> screenIdMap = new HashMap<>();

        for (WorkflowTransitionEntity transition : sourceTransitions) {
            Long sourceScreenId = transition.getScreenId();
            if (sourceScreenId != null && !screenIdMap.containsKey(sourceScreenId)) {
                screenIdMap.put(
                        sourceScreenId,
                        screenCloner.cloneScreenBySourceId(sourceScreenId, tenantId, userId, cloneMode, context)
                );
            }
        }

        return screenIdMap;
    }

    private Map<Long, Long> cloneTransitions(List<WorkflowTransitionEntity> sourceTransitions,
                                             Long targetWorkflowVersionId,
                                             Long tenantId,
                                             Long userId,
                                             Map<Long, Long> stepIdMap,
                                             Map<Long, Long> screenIdMap) {
        Map<Long, Long> transitionIdMap = new HashMap<>();

        if (sourceTransitions.isEmpty()) {
            return transitionIdMap;
        }

        long now = System.currentTimeMillis();
        List<WorkflowTransitionEntity> clonedTransitions = new ArrayList<>();
        for (WorkflowTransitionEntity transition : sourceTransitions) {
            clonedTransitions.add(WorkflowTransitionEntity.builder()
                    .tenantId(tenantId)
                    .workflowVersionId(targetWorkflowVersionId)
                    .name(transition.getName())
                    .fromStepId(requireMappedId(stepIdMap, transition.getFromStepId(), "workflow step"))
                    .toStepId(requireMappedId(stepIdMap, transition.getToStepId(), "workflow step"))
                    .screenId(requireMappedId(screenIdMap, transition.getScreenId(), "screen"))
                    .sequence(transition.getSequence())
                    .createdAt(now)
                    .createdBy(userId)
                    .build());
        }

        List<WorkflowTransitionEntity> savedTransitions =
                workflowTransitionPort.createWorkflowTransitions(clonedTransitions);

        for (int i = 0; i < sourceTransitions.size(); i++) {
            transitionIdMap.put(sourceTransitions.get(i).getId(), savedTransitions.get(i).getId());
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

        List<WorkflowTransitionRuleEntity> clonedRules = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (WorkflowTransitionEntity sourceTransition : sourceTransitions) {
            List<WorkflowTransitionRuleEntity> sourceRules = workflowTransitionRulePort
                    .getWorkflowTransitionRulesByTransitionIdIncludingSystem(sourceTransition.getId(), tenantId);

            Long targetTransitionId = requireMappedId(
                    transitionIdMap,
                    sourceTransition.getId(),
                    "transition"
            );

            for (WorkflowTransitionRuleEntity rule : sourceRules) {
                clonedRules.add(WorkflowTransitionRuleEntity.builder()
                        .tenantId(tenantId)
                        .transitionId(targetTransitionId)
                        .ruleStage(rule.getRuleStage())
                        .ruleKey(rule.getRuleKey())
                        .configJson(rule.getConfigJson())
                        .sequence(rule.getSequence())
                        .isEnabled(rule.getIsEnabled())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }
        }

        if (!clonedRules.isEmpty()) {
            workflowTransitionRulePort.createWorkflowTransitionRules(clonedRules);
        }
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId, String entityName) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing " + entityName + " mapping for source id=" + sourceId
            );
        }
        return mappedId;
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    fieldName + " is required"
            );
        }
    }
}
