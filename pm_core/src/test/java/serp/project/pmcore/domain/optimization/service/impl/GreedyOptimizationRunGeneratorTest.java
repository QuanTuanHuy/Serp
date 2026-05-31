/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.CapacityWorkloadBucket;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateSkillFit;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyGraph;
import serp.project.pmcore.domain.optimization.model.OptimizationDuration;
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.model.OptimizationPriorityScore;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunIntent;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.service.assignment.GreedyAssignmentPolicy;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.BalancedOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.MinimalReassignmentOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.assignment.scoring.SkillFirstOptimizationAssignmentScoringStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.GreedySchedulingPolicy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.BalancedOptimizationSchedulingPriorityStrategy;
import serp.project.pmcore.domain.optimization.service.schedule.priority.DeadlineFirstOptimizationSchedulingPriorityStrategy;
import serp.project.pmcore.domain.optimization.service.summary.OptimizationSummaryBuilder;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreedyOptimizationRunGeneratorTest {
    private static final long START = 1_714_876_800_000L;
    private static final long END = START + 5 * 86_400_000L;
    private static final long HOUR = 3_600_000L;

    private final GreedyOptimizationRunGenerator generator = new GreedyOptimizationRunGenerator(
            new GreedyAssignmentPolicy(),
            new GreedySchedulingPolicy(),
            new OptimizationSummaryBuilder()
    );

    @Test
    void generateShouldRespectNoReassignment() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, 200L)), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.SCHEDULE_ONLY,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertEquals(100L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldScheduleSuccessorAfterPredecessor() {
        WorkItemEntity predecessor = workItem(10L, 100L, null);
        WorkItemEntity successor = workItem(20L, 100L, null);
        OptimizationDependencyGraph graph = dependencyGraph(10L, 20L);
        OptimizationProjectModel model = model(List.of(optimizationItem(predecessor, 100L), optimizationItem(successor, 100L)), graph);

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        OptimizationScheduleSuggestion predecessorSchedule = result.scheduleSuggestions().get(10L);
        OptimizationScheduleSuggestion successorSchedule = result.scheduleSuggestions().get(20L);
        assertNotNull(predecessorSchedule);
        assertNotNull(successorSchedule);
        assertTrue(successorSchedule.plannedStart() >= predecessorSchedule.plannedEnd());
    }

    @Test
    void generateShouldSkipScheduleWhenCycleExists() {
        WorkItemEntity first = workItem(10L, 100L, null);
        WorkItemEntity second = workItem(20L, 100L, null);
        OptimizationDependencyGraph graph = new OptimizationDependencyGraph(
                List.of(new OptimizationDependencyEdge(10L, 20L, 1L, 1L, false),
                        new OptimizationDependencyEdge(20L, 10L, 2L, 1L, false)),
                List.of(),
                List.of(List.of(10L, 20L)),
                Map.of(10L, Set.of(20L), 20L, Set.of(10L)),
                Map.of(10L, Set.of(20L), 20L, Set.of(10L)),
                List.of()
        );
        OptimizationProjectModel model = model(List.of(optimizationItem(first, 100L), optimizationItem(second, 100L)), graph);

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertTrue(result.scheduleSuggestions().isEmpty());
    }

    @Test
    void generateShouldAddCapacitySourceReasonsToScheduleSuggestions() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, 100L)), graphWithoutDependencies(List.of(10L)),
                capacityResolutionWithWorkload());

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        List<String> reasons = result.scheduleSuggestions().get(10L).reasons();
        assertTrue(reasons.contains("Fallback calendar capacity used for assignee"));
        assertTrue(reasons.contains("Existing work_item_plans workload deducted before scheduling"));
        assertEquals(CapacitySourceMode.FALLBACK_WITH_WORKLOAD.name(), result.summary().getCapacitySourceMode());
        assertEquals(2 * HOUR, result.summary().getCrossProjectDeductedMillis());
    }

    @Test
    void generateShouldTrackEffortSeparatelyFromCalendarWindowAcrossSlots() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(
                List.of(optimizationItem(item, List.of(candidate(item, 100L, 1D, true, null)), 10 * HOUR)),
                graphWithoutDependencies(List.of(10L)),
                capacityResolution(List.of(
                        new ResourceCapacitySlot(100L, START, START + 8 * HOUR, 8 * HOUR),
                        new ResourceCapacitySlot(100L, START + 24 * HOUR, START + 32 * HOUR, 8 * HOUR)
                ))
        );

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        OptimizationScheduleSuggestion schedule = result.scheduleSuggestions().get(10L);
        assertEquals(10 * HOUR, schedule.allocatedEffortMillis());
        assertTrue(schedule.plannedEnd() - schedule.plannedStart() > schedule.allocatedEffortMillis());
        assertEquals(2, schedule.allocations().size());
        assertEquals(START, schedule.allocations().get(0).start());
        assertEquals(START + 8 * HOUR, schedule.allocations().get(0).end());
        assertEquals(8 * HOUR, schedule.allocations().get(0).effortMillis());
        assertEquals(START + 24 * HOUR, schedule.allocations().get(1).start());
        assertEquals(START + 26 * HOUR, schedule.allocations().get(1).end());
        assertEquals(2 * HOUR, schedule.allocations().get(1).effortMillis());
        assertEquals(0, result.summary().getOverloadedAssigneeCountAfter());
    }

    @Test
    void generateShouldNotUseCapacityBeforeMidSlotEarliestStart() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(
                List.of(optimizationItem(item, List.of(candidate(item, 100L, 1D, true, null)), 6 * HOUR)),
                graphWithoutDependencies(List.of(10L)),
                capacityResolution(List.of(
                        new ResourceCapacitySlot(100L, START, START + 8 * HOUR, 8 * HOUR),
                        new ResourceCapacitySlot(100L, START + 24 * HOUR, START + 32 * HOUR, 8 * HOUR)
                )),
                Map.of(10L, START + 4 * HOUR)
        );

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        OptimizationScheduleSuggestion schedule = result.scheduleSuggestions().get(10L);
        assertEquals(START + 4 * HOUR, schedule.plannedStart());
        assertEquals(6 * HOUR, schedule.allocatedEffortMillis());
        assertEquals(START + 26 * HOUR, schedule.plannedEnd());
    }

    @Test
    void generateShouldBackfillUnusedCapacityBeforeMidSlotReservation() {
        WorkItemEntity delayedHighPriority = workItem(10L, 100L, null);
        WorkItemEntity normalPriority = workItem(20L, 100L, null);
        OptimizationProjectModel model = model(
                List.of(
                        optimizationItem(delayedHighPriority,
                                List.of(candidate(delayedHighPriority, 100L, 1D, true, null)),
                                4 * HOUR,
                                100D),
                        optimizationItem(normalPriority,
                                List.of(candidate(normalPriority, 100L, 1D, true, null)),
                                4 * HOUR,
                                1D)
                ),
                graphWithoutDependencies(List.of(10L, 20L)),
                capacityResolution(List.of(new ResourceCapacitySlot(100L, START, START + 8 * HOUR, 8 * HOUR))),
                Map.of(10L, START + 4 * HOUR)
        );

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        OptimizationScheduleSuggestion delayedSchedule = result.scheduleSuggestions().get(10L);
        OptimizationScheduleSuggestion normalSchedule = result.scheduleSuggestions().get(20L);
        assertEquals(START + 4 * HOUR, delayedSchedule.plannedStart());
        assertEquals(START + 8 * HOUR, delayedSchedule.plannedEnd());
        assertEquals(START, normalSchedule.plannedStart());
        assertEquals(START + 4 * HOUR, normalSchedule.plannedEnd());
    }

    @Test
    void generateShouldUseRequiredSkillMatchToBeatProjectMemberFallback() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, List.of(
                candidate(item, 100L, 1D, true, null),
                candidate(item, 200L, 1D, false, fullRequiredFit(item.getId(), 200L))
        ))), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertEquals(200L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
        assertTrue(result.assignmentSuggestions().get(10L).reasons().contains("Candidate matches 1/1 required skills"));
    }

    @Test
    void generateShouldPenalizeMissingRequiredSkillButKeepCandidateEligible() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, List.of(
                candidate(item, 200L, 1D, false, missingRequiredFit(item.getId(), 200L))
        ))), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertEquals(200L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
        assertTrue(result.assignmentSuggestions().get(10L).violations().stream()
                .anyMatch(violation -> violation.code() == OptimizationWarningCode.REQUIRED_SKILL_MISSING));
    }

    @Test
    void generateShouldLetCapacityPenaltyBeatSkillBonus() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(longOptimizationItem(item, List.of(
                candidate(item, 100L, 1D, true, null),
                candidate(item, 200L, 1D, false, fullRequiredFit(item.getId(), 200L))
        ))), graphWithoutDependencies(List.of(10L)), capacityForAssignees(40 * HOUR, 0L));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertEquals(100L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldKeepCurrentAssigneeWhenReassignmentDoesNotImproveCost() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, List.of(
                candidate(item, 100L, 1D, true, fullPreferredFit(item.getId(), 100L)),
                candidate(item, 200L, 1D, false, fullPreferredFit(item.getId(), 200L))
        ))), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy()));

        assertEquals(100L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldKeepCurrentAssigneeInMinimalReassignmentStrategy() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, List.of(
                candidate(item, 100L, 5D, true, null),
                candidate(item, 200L, 1D, false, null)
        ))), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new MinimalReassignmentOptimizationAssignmentScoringStrategy()));

        assertEquals(100L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldPreferRequiredSkillMatchInSkillFirstStrategy() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, List.of(
                candidate(item, 100L, 1D, true, null),
                candidate(item, 200L, 32D, false, fullRequiredFit(item.getId(), 200L))
        ))), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new SkillFirstOptimizationAssignmentScoringStrategy()));

        assertEquals(200L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldScheduleEarlierDueDateFirstInDeadlineFirstStrategy() {
        WorkItemEntity highPriorityLaterDue = workItem(10L, 100L, START + 8 * HOUR);
        WorkItemEntity lowPriorityEarlierDue = workItem(20L, 100L, START + 4 * HOUR);
        OptimizationProjectModel model = model(List.of(
                optimizationItem(highPriorityLaterDue,
                        List.of(candidate(highPriorityLaterDue, 100L, 1D, true, null)),
                        4 * HOUR,
                        100D),
                optimizationItem(lowPriorityEarlierDue,
                        List.of(candidate(lowPriorityEarlierDue, 100L, 1D, true, null)),
                        4 * HOUR,
                        1D)
        ), graphWithoutDependencies(List.of(10L, 20L)));

        OptimizationGenerationResult result = generator.generate(model,
                options(OptimizationObjective.BALANCED_WORKLOAD, OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE,
                        new BalancedOptimizationAssignmentScoringStrategy(),
                        new DeadlineFirstOptimizationSchedulingPriorityStrategy()));

        assertEquals(START, result.scheduleSuggestions().get(20L).plannedStart());
        assertEquals(START + 4 * HOUR, result.scheduleSuggestions().get(10L).plannedStart());
    }

    private OptimizationAlgorithmOptions options(OptimizationObjective objective,
                                                 OptimizationChangeScope changeScope,
                                                 serp.project.pmcore.domain.optimization.service.assignment.scoring.OptimizationAssignmentScoringStrategy scoringStrategy) {
        return options(objective, changeScope, scoringStrategy, new BalancedOptimizationSchedulingPriorityStrategy());
    }

    private OptimizationAlgorithmOptions options(OptimizationObjective objective,
                                                 OptimizationChangeScope changeScope,
                                                 serp.project.pmcore.domain.optimization.service.assignment.scoring.OptimizationAssignmentScoringStrategy scoringStrategy,
                                                 serp.project.pmcore.domain.optimization.service.schedule.priority.OptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        return new OptimizationAlgorithmOptions(
                new OptimizationRunIntent(OptimizationAlgorithmKeys.GREEDY_BALANCED, objective, changeScope),
                scoringStrategy,
                schedulingPriorityStrategy
        );
    }

    private OptimizationProjectModel model(List<OptimizationWorkItem> items, OptimizationDependencyGraph graph) {
        return model(items, graph, capacityResolution());
    }

    private OptimizationProjectModel model(List<OptimizationWorkItem> items,
                                           OptimizationDependencyGraph graph,
                                           CapacityResolutionResult capacityResolution) {
        return model(items, graph, capacityResolution, Map.of());
    }

    private OptimizationProjectModel model(List<OptimizationWorkItem> items,
                                           OptimizationDependencyGraph graph,
                                           CapacityResolutionResult capacityResolution,
                                           Map<Long, Long> earliestStartByWorkItemId) {
        return new OptimizationProjectModel(
                1L,
                100L,
                ProjectEntity.builder().id(100L).tenantId(1L).leadUserId(100L).build(),
                START,
                END,
                graph,
                items,
                capacityResolution.slots(),
                capacityResolution,
                List.of(),
                earliestStartByWorkItemId
        );
    }

    private CapacityResolutionResult capacityResolution() {
        return capacityResolution(List.of(new ResourceCapacitySlot(100L, START, START + 86_400_000L, 8 * HOUR),
                new ResourceCapacitySlot(200L, START, START + 86_400_000L, 8 * HOUR)));
    }

    private CapacityResolutionResult capacityResolution(List<ResourceCapacitySlot> slots) {
        return new CapacityResolutionResult(
                slots,
                CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                CapacityCoverageStatus.MISSING,
                CapacityCoverageStatus.NOT_REQUIRED,
                List.of(100L, 200L),
                null,
                START,
                0L,
                0L,
                0L,
                List.of(),
                List.of()
        );
    }

    private CapacityResolutionResult capacityResolutionWithWorkload() {
        return new CapacityResolutionResult(
                List.of(new ResourceCapacitySlot(100L, START, START + 86_400_000L, 6 * HOUR)),
                CapacitySourceMode.FALLBACK_WITH_WORKLOAD,
                CapacityCoverageStatus.MISSING,
                CapacityCoverageStatus.FULL,
                List.of(100L),
                null,
                START,
                2 * HOUR,
                0L,
                2 * HOUR,
                List.of(new CapacityWorkloadBucket(100L, START, START + 86_400_000L, 0L, 2 * HOUR, 2 * HOUR)),
                List.of()
        );
    }

    private CapacityResolutionResult capacityForAssignees(long firstCapacity, long secondCapacity) {
        return new CapacityResolutionResult(
                List.of(new ResourceCapacitySlot(100L, START, START + 86_400_000L, firstCapacity),
                        new ResourceCapacitySlot(200L, START, START + 86_400_000L, secondCapacity)),
                CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                CapacityCoverageStatus.MISSING,
                CapacityCoverageStatus.NOT_REQUIRED,
                List.of(100L, 200L),
                null,
                START,
                0L,
                0L,
                0L,
                List.of(),
                List.of()
        );
    }

    private OptimizationWorkItem optimizationItem(WorkItemEntity workItem, Long candidateId) {
        return optimizationItem(workItem, List.of(candidate(workItem, candidateId, 1D,
                candidateId.equals(workItem.getAssigneeId()), null)));
    }

    private OptimizationWorkItem optimizationItem(WorkItemEntity workItem, List<OptimizationCandidateAssignee> candidates) {
        return optimizationItem(workItem, candidates, HOUR);
    }

    private OptimizationWorkItem optimizationItem(WorkItemEntity workItem,
                                                  List<OptimizationCandidateAssignee> candidates,
                                                  long durationMillis) {
        return optimizationItem(workItem, candidates, durationMillis, 1D);
    }

    private OptimizationWorkItem optimizationItem(WorkItemEntity workItem,
                                                  List<OptimizationCandidateAssignee> candidates,
                                                  long durationMillis,
                                                  double priorityScore) {
        return new OptimizationWorkItem(
                workItem,
                null,
                new OptimizationDuration(workItem.getId(), durationMillis, OptimizationConfidence.HIGH, "TEST"),
                new OptimizationPriorityScore(workItem.getId(), priorityScore, false),
                candidates,
                false,
                false
        );
    }

    private OptimizationWorkItem longOptimizationItem(WorkItemEntity workItem, List<OptimizationCandidateAssignee> candidates) {
        return new OptimizationWorkItem(
                workItem,
                null,
                new OptimizationDuration(workItem.getId(), 60 * HOUR, OptimizationConfidence.HIGH, "TEST"),
                new OptimizationPriorityScore(workItem.getId(), 1D, false),
                candidates,
                false,
                false
        );
    }

    private OptimizationCandidateAssignee candidate(WorkItemEntity workItem,
                                                    Long candidateId,
                                                    double baseCost,
                                                    boolean currentAssignee,
                                                    OptimizationCandidateSkillFit skillFit) {
        return new OptimizationCandidateAssignee(workItem.getId(), candidateId, baseCost,
                currentAssignee, false, false, false, !currentAssignee, skillFit);
    }

    private OptimizationCandidateSkillFit fullRequiredFit(Long workItemId, Long candidateId) {
        return new OptimizationCandidateSkillFit(workItemId, candidateId, 1, 1, 0, 0, 100D, 100D,
                4D, List.of(), List.of(), List.of(501L), OptimizationConfidence.HIGH);
    }

    private OptimizationCandidateSkillFit missingRequiredFit(Long workItemId, Long candidateId) {
        return new OptimizationCandidateSkillFit(workItemId, candidateId, 0, 1, 0, 0, 0D, 100D,
                0D, List.of(501L), List.of(), List.of(), OptimizationConfidence.MEDIUM);
    }

    private OptimizationCandidateSkillFit partialPreferredFit(Long workItemId, Long candidateId) {
        return new OptimizationCandidateSkillFit(workItemId, candidateId, 0, 0, 1, 2, 100D, 50D,
                1D, List.of(), List.of(602L), List.of(601L), OptimizationConfidence.MEDIUM);
    }

    private OptimizationCandidateSkillFit fullPreferredFit(Long workItemId, Long candidateId) {
        return new OptimizationCandidateSkillFit(workItemId, candidateId, 0, 0, 2, 2, 100D, 100D,
                2D, List.of(), List.of(), List.of(601L, 602L), OptimizationConfidence.HIGH);
    }

    private WorkItemEntity workItem(Long id, Long assigneeId, Long dueDate) {
        return WorkItemEntity.builder()
                .id(id)
                .tenantId(1L)
                .projectId(100L)
                .assigneeId(assigneeId)
                .dueDate(dueDate)
                .prioritySequence(1)
                .rank(String.valueOf(id))
                .build();
    }

    private OptimizationDependencyGraph graphWithoutDependencies(List<Long> ids) {
        Map<Long, Set<Long>> empty = new LinkedHashMap<>();
        ids.forEach(id -> empty.put(id, Set.of()));
        return new OptimizationDependencyGraph(List.of(), List.of(), List.of(), empty, empty, ids);
    }

    private OptimizationDependencyGraph dependencyGraph(Long predecessor, Long successor) {
        Map<Long, Set<Long>> predecessors = new LinkedHashMap<>();
        predecessors.put(predecessor, Set.of());
        predecessors.put(successor, Set.of(predecessor));
        Map<Long, Set<Long>> successors = new LinkedHashMap<>();
        successors.put(predecessor, Set.of(successor));
        successors.put(successor, Set.of());
        return new OptimizationDependencyGraph(
                List.of(new OptimizationDependencyEdge(predecessor, successor, 1L, 1L, false)),
                List.of(),
                List.of(),
                predecessors,
                successors,
                List.of(predecessor, successor)
        );
    }
}
