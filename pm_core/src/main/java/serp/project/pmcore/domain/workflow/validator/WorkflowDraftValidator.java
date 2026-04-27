/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationFinding;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationSeverity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkflowDraftValidator {

    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IStatusService statusService;

    public WorkflowValidationResult validateDraft(Long workflowVersionId, Long tenantId) {
        List<WorkflowStepEntity> steps = workflowStepPort.getWorkflowStepsByWorkflowVersionId(workflowVersionId, tenantId);
        List<WorkflowTransitionEntity> transitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionId(workflowVersionId, tenantId);

        List<WorkflowValidationFinding> errors = new ArrayList<>();
        List<WorkflowValidationFinding> warnings = new ArrayList<>();
        Map<Long, WorkflowStepEntity> stepsById = new HashMap<>();
        for (WorkflowStepEntity step : steps) {
            stepsById.put(step.getId(), step);
        }

        List<WorkflowStepEntity> initialSteps = steps.stream()
                .filter(step -> Boolean.TRUE.equals(step.getIsInitial()))
                .toList();
        if (initialSteps.size() != 1) {
            errors.add(error(
                    "V-001",
                    initialSteps.isEmpty()
                            ? "Workflow must have exactly one initial step but found none"
                            : "Workflow must have exactly one initial step but found " + initialSteps.size()
            ));
        }

        long terminalSteps = steps.stream()
                .filter(step -> Boolean.TRUE.equals(step.getIsTerminal()))
                .count();
        if (terminalSteps == 0) {
            errors.add(error("V-002", "Workflow must have at least one final step"));
        }

        validateStatuses(steps, tenantId, errors);
        validateTransitionReferences(transitions, stepsById, errors);

        if (initialSteps.size() == 1) {
            validateReachability(initialSteps.getFirst(), steps, transitions, errors);
        }

        validateOrphans(steps, transitions, warnings);
        validateSelfLoops(transitions, warnings);

        return new WorkflowValidationResult(errors, warnings);
    }

    private void validateStatuses(List<WorkflowStepEntity> steps,
                                  Long tenantId,
                                  List<WorkflowValidationFinding> errors) {
        for (WorkflowStepEntity step : steps) {
            try {
                statusService.getVisibleStatusById(step.getStatusId(), tenantId);
            } catch (ResourceNotFoundException ex) {
                errors.add(error(
                        "V-005",
                        "Workflow step references missing status: stepId=" + step.getId() + ", statusId=" + step.getStatusId()
                ));
            }
        }
    }

    private void validateTransitionReferences(List<WorkflowTransitionEntity> transitions,
                                              Map<Long, WorkflowStepEntity> stepsById,
                                              List<WorkflowValidationFinding> errors) {
        for (WorkflowTransitionEntity transition : transitions) {
            if (transition.getFromStepId() != null && !stepsById.containsKey(transition.getFromStepId())) {
                errors.add(error(
                        "V-003",
                        "Transition references source step outside draft version: transitionId=" + transition.getId()
                                + ", fromStepId=" + transition.getFromStepId()
                ));
            }

            if (transition.getToStepId() == null || !stepsById.containsKey(transition.getToStepId())) {
                errors.add(error(
                        "V-003",
                        "Transition references target step outside draft version: transitionId=" + transition.getId()
                                + ", toStepId=" + transition.getToStepId()
                ));
            }
        }
    }

    private void validateReachability(WorkflowStepEntity initialStep,
                                      List<WorkflowStepEntity> steps,
                                      List<WorkflowTransitionEntity> transitions,
                                      List<WorkflowValidationFinding> errors) {
        Set<Long> reachableStepIds = collectReachableStepIds(initialStep.getId(), transitions);
        for (WorkflowStepEntity step : steps) {
            if (Boolean.TRUE.equals(step.getIsInitial())) {
                continue;
            }
            if (!reachableStepIds.contains(step.getId())) {
                errors.add(error(
                        "V-003",
                        "Non-initial step is unreachable from initial step: stepId=" + step.getId()
                ));
            }
        }
    }

    private Set<Long> collectReachableStepIds(Long initialStepId, List<WorkflowTransitionEntity> transitions) {
        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        visited.add(initialStepId);
        queue.add(initialStepId);

        while (!queue.isEmpty()) {
            Long currentStepId = queue.removeFirst();
            for (WorkflowTransitionEntity transition : transitions) {
                if (!isAvailableFromStep(transition, currentStepId) || transition.getToStepId() == null) {
                    continue;
                }
                if (visited.add(transition.getToStepId())) {
                    queue.addLast(transition.getToStepId());
                }
            }
        }

        return visited;
    }

    private void validateOrphans(List<WorkflowStepEntity> steps,
                                 List<WorkflowTransitionEntity> transitions,
                                 List<WorkflowValidationFinding> warnings) {
        for (WorkflowStepEntity step : steps) {
            boolean hasIncoming = transitions.stream()
                    .anyMatch(transition -> Objects.equals(step.getId(), transition.getToStepId()));
            boolean hasOutgoing = transitions.stream()
                    .anyMatch(transition -> isAvailableFromStep(transition, step.getId()));
            if (!hasIncoming && !hasOutgoing) {
                warnings.add(warning(
                        "V-004",
                        "Workflow step has no incoming or outgoing transitions: stepId=" + step.getId()
                ));
            }
        }
    }

    private void validateSelfLoops(List<WorkflowTransitionEntity> transitions,
                                   List<WorkflowValidationFinding> warnings) {
        for (WorkflowTransitionEntity transition : transitions) {
            if (transition.getFromStepId() != null && Objects.equals(transition.getFromStepId(), transition.getToStepId())) {
                warnings.add(warning(
                        "V-006",
                        "Workflow transition is a self-loop: transitionId=" + transition.getId()
                ));
            }
        }
    }

    private boolean isAvailableFromStep(WorkflowTransitionEntity transition, Long stepId) {
        return transition.getFromStepId() == null || Objects.equals(transition.getFromStepId(), stepId);
    }

    private WorkflowValidationFinding error(String ruleKey, String message) {
        return new WorkflowValidationFinding(ruleKey, WorkflowValidationSeverity.ERROR, message);
    }

    private WorkflowValidationFinding warning(String ruleKey, String message) {
        return new WorkflowValidationFinding(ruleKey, WorkflowValidationSeverity.WARNING, message);
    }
}
