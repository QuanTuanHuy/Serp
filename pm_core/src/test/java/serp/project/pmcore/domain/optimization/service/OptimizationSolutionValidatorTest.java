/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyGraph;
import serp.project.pmcore.domain.optimization.model.OptimizationDuration;
import serp.project.pmcore.domain.optimization.model.OptimizationPriorityScore;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationRunIntent;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizationSolutionValidatorTest {
    private final OptimizationSolutionValidator validator = new OptimizationSolutionValidator();

    @Test
    void validateShouldWarnWhenReassignmentDisabledButAssigneeChanges() {
        OptimizationProblem problem = problem(
                input(OptimizationChangeScope.SCHEDULE_ONLY),
                graph(List.of(10L), List.of()),
                Map.of(),
                item(10L, 100L, List.of(candidate(10L, 200L)))
        );
        OptimizationSolution solution = solution(
                Map.of(10L, assignment(10L, 200L)),
                Map.of()
        );

        OptimizationSolution validated = validator.validate(problem, solution);

        assertThat(validated.warnings())
                .anyMatch(warning -> warning.code() == OptimizationWarningCode.INVALID_OVERRIDE
                        && warning.workItemId().equals(10L)
                        && warning.message().contains("reassignment is disabled"));
    }

    @Test
    void validateShouldWarnWhenScheduleChangesDisabledButScheduleExists() {
        OptimizationProblem problem = problem(
                input(OptimizationChangeScope.ASSIGNMENT_ONLY),
                graph(List.of(10L), List.of()),
                Map.of(),
                item(10L, 100L, List.of(candidate(10L, 100L)))
        );
        OptimizationSolution solution = solution(
                Map.of(),
                Map.of(10L, schedule(10L, 100L, 1_000L, 2_000L))
        );

        OptimizationSolution validated = validator.validate(problem, solution);

        assertThat(validated.warnings())
                .anyMatch(warning -> warning.code() == OptimizationWarningCode.INVALID_OVERRIDE
                        && warning.workItemId().equals(10L)
                        && warning.message().contains("schedule changes are disabled"));
    }

    @Test
    void validateShouldWarnWhenScheduleRangeIsInvalid() {
        OptimizationProblem problem = problem(
                input(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE),
                graph(List.of(10L), List.of()),
                Map.of(),
                item(10L, 100L, List.of(candidate(10L, 100L)))
        );
        OptimizationSolution solution = solution(
                Map.of(),
                Map.of(10L, schedule(10L, 100L, 2_000L, 2_000L))
        );

        OptimizationSolution validated = validator.validate(problem, solution);

        assertThat(validated.warnings())
                .anyMatch(warning -> warning.code() == OptimizationWarningCode.INVALID_OVERRIDE
                        && warning.workItemId().equals(10L)
                        && warning.message().contains("invalid planned range"));
    }

    @Test
    void validateShouldWarnWhenSuccessorStartsBeforePredecessorEnds() {
        OptimizationDependencyEdge edge = new OptimizationDependencyEdge(10L, 20L, 1L, 2L, false);
        OptimizationProblem problem = problem(
                input(OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE),
                graph(List.of(10L, 20L), List.of(edge)),
                Map.of(),
                item(10L, 100L, List.of(candidate(10L, 100L))),
                item(20L, 100L, List.of(candidate(20L, 100L)))
        );
        OptimizationSolution solution = solution(
                Map.of(),
                Map.of(
                        10L, schedule(10L, 100L, 1_000L, 3_000L),
                        20L, schedule(20L, 100L, 2_000L, 4_000L)
                )
        );

        OptimizationSolution validated = validator.validate(problem, solution);

        assertThat(validated.warnings())
                .anyMatch(warning -> warning.code() == OptimizationWarningCode.DEPENDENCY_VIOLATION
                        && warning.workItemId().equals(20L)
                        && warning.message().contains("before predecessor finishes"));
    }

    private OptimizationBuilderInput input(OptimizationChangeScope changeScope) {
        return new OptimizationBuilderInput(
                1L,
                100L,
                List.of(10L, 20L),
                1_000L,
                10_000L,
                new OptimizationRunIntent(
                        OptimizationAlgorithmKeys.GREEDY_BALANCED,
                        OptimizationObjective.BALANCED_WORKLOAD,
                        changeScope
                )
        );
    }

    private OptimizationProblem problem(OptimizationBuilderInput input,
                                        OptimizationDependencyGraph graph,
                                        Map<Long, Long> earliestStarts,
                                        OptimizationWorkItem... items) {
        OptimizationProjectModel projectModel = new OptimizationProjectModel(
                input.tenantId(),
                input.projectId(),
                null,
                input.planningStart(),
                input.planningEnd(),
                graph,
                List.of(items),
                List.of(),
                capacityResolution(),
                List.of(),
                earliestStarts
        );
        return new OptimizationProblem(projectModel, input);
    }

    private OptimizationDependencyGraph graph(List<Long> ids, List<OptimizationDependencyEdge> edges) {
        Map<Long, Set<Long>> predecessors = ids.stream().collect(java.util.stream.Collectors.toMap(
                id -> id,
                id -> edges.stream()
                        .filter(edge -> edge.successorId().equals(id))
                        .map(OptimizationDependencyEdge::predecessorId)
                        .collect(java.util.stream.Collectors.toSet())
        ));
        Map<Long, Set<Long>> successors = ids.stream().collect(java.util.stream.Collectors.toMap(
                id -> id,
                id -> edges.stream()
                        .filter(edge -> edge.predecessorId().equals(id))
                        .map(OptimizationDependencyEdge::successorId)
                        .collect(java.util.stream.Collectors.toSet())
        ));
        return new OptimizationDependencyGraph(edges, List.of(), List.of(), predecessors, successors, ids);
    }

    private OptimizationWorkItem item(Long workItemId,
                                      Long assigneeId,
                                      List<OptimizationCandidateAssignee> candidates) {
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(workItemId)
                .tenantId(1L)
                .projectId(100L)
                .assigneeId(assigneeId)
                .build();
        return new OptimizationWorkItem(
                workItem,
                null,
                new OptimizationDuration(workItemId, OptimizationConstants.HOUR_MILLIS, OptimizationConfidence.HIGH, "TEST"),
                new OptimizationPriorityScore(workItemId, 1D, false),
                candidates,
                false,
                false
        );
    }

    private OptimizationCandidateAssignee candidate(Long workItemId, Long candidateId) {
        return new OptimizationCandidateAssignee(
                workItemId,
                candidateId,
                1D,
                false,
                false,
                false,
                false,
                true,
                null
        );
    }

    private OptimizationAssignmentSuggestion assignment(Long workItemId, Long suggestedAssigneeId) {
        return new OptimizationAssignmentSuggestion(workItemId, suggestedAssigneeId, 1D, List.of(), List.of());
    }

    private OptimizationScheduleSuggestion schedule(Long workItemId, Long assigneeId, Long plannedStart, Long plannedEnd) {
        return new OptimizationScheduleSuggestion(
                workItemId,
                assigneeId,
                plannedStart,
                plannedEnd,
                plannedEnd - plannedStart,
                List.of(),
                OptimizationConfidence.HIGH,
                List.of(),
                List.of()
        );
    }

    private OptimizationSolution solution(Map<Long, OptimizationAssignmentSuggestion> assignments,
                                          Map<Long, OptimizationScheduleSuggestion> schedules) {
        return new OptimizationSolution(
                assignments,
                schedules,
                List.of(),
                OptimizationRunSummary.builder().scopeSize(assignments.size() + schedules.size()).build(),
                new OptimizationAlgorithmDescriptor(
                        OptimizationAlgorithmKeys.GREEDY_BALANCED,
                        OptimizationAlgorithmKeys.DEFAULT_VERSION,
                        Set.of(OptimizationCapability.ASSIGNMENT, OptimizationCapability.SCHEDULING)
                ),
                OptimizationSolverStatus.FEASIBLE,
                null
        );
    }

    private CapacityResolutionResult capacityResolution() {
        return new CapacityResolutionResult(
                List.of(),
                CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                CapacityCoverageStatus.NOT_REQUIRED,
                CapacityCoverageStatus.NOT_REQUIRED,
                List.of(),
                null,
                null,
                0L,
                0L,
                0L,
                List.of(),
                List.of()
        );
    }
}
