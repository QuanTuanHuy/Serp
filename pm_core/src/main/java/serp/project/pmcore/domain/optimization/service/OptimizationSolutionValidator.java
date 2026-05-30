/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OptimizationSolutionValidator {
    public OptimizationSolution validate(OptimizationProblem problem, OptimizationSolution solution) {
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(solution.warnings());
        Map<Long, OptimizationWorkItem> itemById = problem.projectModel().workItems().stream()
                .collect(Collectors.toMap(item -> item.workItem().getId(), Function.identity(), (left, right) -> left));

        validateMissingSuggestions(solution, itemById, warnings);
        validateAssignments(problem, solution.assignmentSuggestions(), itemById, warnings);
        validateSchedules(problem, solution.scheduleSuggestions(), warnings);

        return new OptimizationSolution(
                solution.assignmentSuggestions(),
                solution.scheduleSuggestions(),
                warnings,
                solution.summary(),
                solution.algorithm(),
                solution.solverStatus(),
                solution.objectiveScore()
        );
    }

    private void validateMissingSuggestions(OptimizationSolution solution,
                                            Map<Long, OptimizationWorkItem> itemById,
                                            List<OptimizationConstraintViolation> warnings) {
        for (Long workItemId : itemById.keySet()) {
            if (!solution.assignmentSuggestions().containsKey(workItemId)
                    && !solution.scheduleSuggestions().containsKey(workItemId)) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        workItemId,
                        "Optimization solution omitted selected work item",
                        null
                ));
            }
        }
    }

    private void validateAssignments(OptimizationProblem problem,
                                     Map<Long, OptimizationAssignmentSuggestion> assignments,
                                     Map<Long, OptimizationWorkItem> itemById,
                                     List<OptimizationConstraintViolation> warnings) {
        boolean assignmentChangesAllowed = problem.input().intent().changeScope().includesAssignment();
        for (Map.Entry<Long, OptimizationAssignmentSuggestion> entry : assignments.entrySet()) {
            OptimizationWorkItem item = itemById.get(entry.getKey());
            if (item == null) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion references a work item outside the optimization problem",
                        null
                ));
                continue;
            }
            Long currentAssigneeId = item.workItem().getAssigneeId();
            Long suggestedAssigneeId = entry.getValue().suggestedAssigneeId();
            if (!assignmentChangesAllowed && !Objects.equals(currentAssigneeId, suggestedAssigneeId)) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion changes assignee while reassignment is disabled",
                        "currentAssigneeId=" + currentAssigneeId + ", suggestedAssigneeId=" + suggestedAssigneeId
                ));
            }
            Set<Long> candidateIds = item.candidateAssignees().stream()
                    .map(candidate -> candidate.candidateId())
                    .collect(Collectors.toSet());
            if (suggestedAssigneeId != null
                    && !candidateIds.contains(suggestedAssigneeId)
                    && !Objects.equals(currentAssigneeId, suggestedAssigneeId)) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        entry.getKey(),
                        "Assignment suggestion uses an assignee outside the candidate set",
                        "suggestedAssigneeId=" + suggestedAssigneeId
                ));
            }
        }
    }

    private void validateSchedules(OptimizationProblem problem,
                                   Map<Long, OptimizationScheduleSuggestion> schedules,
                                   List<OptimizationConstraintViolation> warnings) {
        boolean scheduleChangesAllowed = problem.input().intent().changeScope().includesScheduling();
        if (!scheduleChangesAllowed && !schedules.isEmpty()) {
            schedules.keySet().forEach(workItemId -> warnings.add(new OptimizationConstraintViolation(
                    OptimizationWarningCode.INVALID_OVERRIDE,
                    workItemId,
                    "Schedule suggestion exists while schedule changes are disabled",
                    null
            )));
        }
        schedules.forEach((workItemId, schedule) -> {
            if (schedule.plannedStart() == null
                    || schedule.plannedEnd() == null
                    || schedule.plannedStart() >= schedule.plannedEnd()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        workItemId,
                        "Schedule suggestion has an invalid planned range",
                        null
                ));
            }
            if (schedule.plannedStart() != null && schedule.plannedStart() < problem.projectModel().planningStart()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.INVALID_OVERRIDE,
                        workItemId,
                        "Schedule suggestion starts before the planning window",
                        "planningStart=" + problem.projectModel().planningStart()
                ));
            }
        });
        problem.projectModel().dependencyGraph().internalEdges().forEach(edge -> {
            OptimizationScheduleSuggestion predecessor = schedules.get(edge.predecessorId());
            OptimizationScheduleSuggestion successor = schedules.get(edge.successorId());
            if (predecessor != null
                    && successor != null
                    && successor.plannedStart() != null
                    && predecessor.plannedEnd() != null
                    && successor.plannedStart() < predecessor.plannedEnd()) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.DEPENDENCY_VIOLATION,
                        edge.successorId(),
                        "Schedule suggestion starts before predecessor finishes",
                        edge.predecessorId() + " -> " + edge.successorId()
                ));
            }
        });
        problem.projectModel().earliestStartByWorkItemId().forEach((workItemId, earliestStart) -> {
            OptimizationScheduleSuggestion schedule = schedules.get(workItemId);
            if (schedule != null && schedule.plannedStart() != null && schedule.plannedStart() < earliestStart) {
                warnings.add(new OptimizationConstraintViolation(
                        OptimizationWarningCode.DEPENDENCY_VIOLATION,
                        workItemId,
                        "Schedule suggestion starts before external dependency earliest start",
                        "earliestStart=" + earliestStart
                ));
            }
        });
    }
}
