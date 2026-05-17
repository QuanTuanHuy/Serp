/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.CapacityWorkloadBucket;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyGraph;
import serp.project.pmcore.domain.optimization.model.OptimizationDuration;
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.model.OptimizationPriorityScore;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
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

    private final GreedyOptimizationRunGenerator generator = new GreedyOptimizationRunGenerator();

    @Test
    void generateShouldRespectNoReassignment() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, 200L)), graphWithoutDependencies(List.of(10L)));

        OptimizationGenerationResult result = generator.generate(model,
                input(false, true, OptimizationMode.BALANCED_WORKLOAD));

        assertEquals(100L, result.assignmentSuggestions().get(10L).suggestedAssigneeId());
    }

    @Test
    void generateShouldScheduleSuccessorAfterPredecessor() {
        WorkItemEntity predecessor = workItem(10L, 100L, null);
        WorkItemEntity successor = workItem(20L, 100L, null);
        OptimizationDependencyGraph graph = dependencyGraph(10L, 20L);
        OptimizationProjectModel model = model(List.of(optimizationItem(predecessor, 100L), optimizationItem(successor, 100L)), graph);

        OptimizationGenerationResult result = generator.generate(model,
                input(true, true, OptimizationMode.BALANCED_WORKLOAD));

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
                input(true, true, OptimizationMode.BALANCED_WORKLOAD));

        assertTrue(result.scheduleSuggestions().isEmpty());
    }

    @Test
    void generateShouldAddCapacitySourceReasonsToScheduleSuggestions() {
        WorkItemEntity item = workItem(10L, 100L, null);
        OptimizationProjectModel model = model(List.of(optimizationItem(item, 100L)), graphWithoutDependencies(List.of(10L)),
                capacityResolutionWithWorkload());

        OptimizationGenerationResult result = generator.generate(model,
                input(true, true, OptimizationMode.BALANCED_WORKLOAD));

        List<String> reasons = result.scheduleSuggestions().get(10L).reasons();
        assertTrue(reasons.contains("Fallback calendar capacity used for assignee"));
        assertTrue(reasons.contains("Existing work_item_plans workload deducted before scheduling"));
        assertEquals(CapacitySourceMode.FALLBACK_WITH_WORKLOAD.name(), result.summary().getCapacitySourceMode());
        assertEquals(2 * HOUR, result.summary().getCrossProjectDeductedMillis());
    }

    private OptimizationBuilderInput input(boolean allowReassignment, boolean allowSchedule, OptimizationMode mode) {
        return new OptimizationBuilderInput(1L, 100L, List.of(10L, 20L), START, END, allowReassignment, allowSchedule, mode);
    }

    private OptimizationProjectModel model(List<OptimizationWorkItem> items, OptimizationDependencyGraph graph) {
        return model(items, graph, capacityResolution());
    }

    private OptimizationProjectModel model(List<OptimizationWorkItem> items,
                                           OptimizationDependencyGraph graph,
                                           CapacityResolutionResult capacityResolution) {
        return new OptimizationProjectModel(
                1L,
                100L,
                ProjectEntity.builder().id(100L).tenantId(1L).leadUserId(100L).build(),
                START,
                END,
                graph,
                items,
                List.of(new ResourceCapacitySlot(100L, START, START + 86_400_000L, 8 * HOUR),
                        new ResourceCapacitySlot(200L, START, START + 86_400_000L, 8 * HOUR)),
                capacityResolution,
                List.of(),
                Map.of()
        );
    }

    private CapacityResolutionResult capacityResolution() {
        return new CapacityResolutionResult(
                List.of(new ResourceCapacitySlot(100L, START, START + 86_400_000L, 8 * HOUR),
                        new ResourceCapacitySlot(200L, START, START + 86_400_000L, 8 * HOUR)),
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

    private OptimizationWorkItem optimizationItem(WorkItemEntity workItem, Long candidateId) {
        return new OptimizationWorkItem(
                workItem,
                null,
                new OptimizationDuration(workItem.getId(), HOUR, OptimizationConfidence.HIGH, "TEST"),
                new OptimizationPriorityScore(workItem.getId(), 1D, false),
                List.of(new OptimizationCandidateAssignee(workItem.getId(), candidateId, 1D,
                        candidateId.equals(workItem.getAssigneeId()), false, false, false, false, null)),
                false,
                false
        );
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
