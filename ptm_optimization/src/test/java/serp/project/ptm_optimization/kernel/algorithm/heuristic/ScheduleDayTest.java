/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.kernel.algorithm.heuristic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Params;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.utils.UtilityModel;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleDayTest {

    private ScheduleDay scheduleDay;
    private UtilityModel utilityModel;
    private Weights defaultWeights;
    private Params defaultParams;
    private Long testDateMs;

    @BeforeEach
    void setUp() {
        utilityModel = new UtilityModel();
        scheduleDay = new ScheduleDay(utilityModel);

        defaultWeights = Weights.builder()
                .wPriority(1.0)
                .wDeadline(0.5)
                .wSwitch(0.2)
                .wFatigue(0.1)
                .wEnjoy(0.1)
                .build();

        defaultParams = Params.builder()
                .slotMin(15)
                .timeBudgetLS(Duration.ofMillis(100))
                .build();

        // Test date: 2025-11-05 00:00:00 UTC
        testDateMs = LocalDate.of(2025, 11, 5)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli();
    }

    // ============ Topological Sort Tests ============

    @Test
    @DisplayName("TopoOrder: Simple linear dependency chain")
    void testTopoOrder_LinearChain() {
        // Task 1 -> Task 2 -> Task 3
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(3L).dependentTaskIds(List.of(2L)).build(),
                TaskInput.builder().taskId(2L).dependentTaskIds(List.of(1L)).build(),
                TaskInput.builder().taskId(1L).dependentTaskIds(List.of()).build());

        Pair<List<TaskInput>, Boolean> result = scheduleDay.topoOrder(tasks);

        assertTrue(result.getSecond(), "Should be acyclic");
        List<TaskInput> ordered = result.getFirst();
        assertEquals(3, ordered.size());

        // Task 1 should come before Task 2, Task 2 before Task 3
        int idx1 = findTaskIndex(ordered, 1L);
        int idx2 = findTaskIndex(ordered, 2L);
        int idx3 = findTaskIndex(ordered, 3L);

        assertTrue(idx1 < idx2, "Task 1 should come before Task 2");
        assertTrue(idx2 < idx3, "Task 2 should come before Task 3");
    }

    @Test
    @DisplayName("TopoOrder: Detect circular dependency")
    void testTopoOrder_CircularDependency() {
        // Task 1 -> Task 2 -> Task 3 -> Task 1 (cycle)
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).dependentTaskIds(List.of(3L)).build(),
                TaskInput.builder().taskId(2L).dependentTaskIds(List.of(1L)).build(),
                TaskInput.builder().taskId(3L).dependentTaskIds(List.of(2L)).build());

        Pair<List<TaskInput>, Boolean> result = scheduleDay.topoOrder(tasks);

        assertFalse(result.getSecond(), "Should detect cycle");
    }

    @Test
    @DisplayName("TopoOrder: Diamond dependency pattern")
    void testTopoOrder_DiamondDependency() {
        // Task 1 -> Task 2, Task 3 -> Task 4
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(4L).dependentTaskIds(List.of(2L, 3L)).build(),
                TaskInput.builder().taskId(3L).dependentTaskIds(List.of(1L)).build(),
                TaskInput.builder().taskId(2L).dependentTaskIds(List.of(1L)).build(),
                TaskInput.builder().taskId(1L).dependentTaskIds(List.of()).build());

        Pair<List<TaskInput>, Boolean> result = scheduleDay.topoOrder(tasks);

        assertTrue(result.getSecond(), "Should be acyclic");
        List<TaskInput> ordered = result.getFirst();

        int idx1 = findTaskIndex(ordered, 1L);
        int idx2 = findTaskIndex(ordered, 2L);
        int idx3 = findTaskIndex(ordered, 3L);
        int idx4 = findTaskIndex(ordered, 4L);

        assertTrue(idx1 < idx2 && idx1 < idx3, "Task 1 before Task 2 and 3");
        assertTrue(idx2 < idx4 && idx3 < idx4, "Task 2 and 3 before Task 4");
    }

    @Test
    @DisplayName("TopoOrder: No dependencies - sort by deadline and priority")
    void testTopoOrder_NoDependencies_SortedByDeadlineAndPriority() {
        Long now = System.currentTimeMillis();

        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).deadlineMs(now + 86400000L).priorityScore(0.5).build(), // 1 day
                TaskInput.builder().taskId(2L).deadlineMs(now + 3600000L).priorityScore(0.8).build(), // 1 hour (urgent)
                TaskInput.builder().taskId(3L).deadlineMs(now + 86400000L).priorityScore(0.9).build() // 1 day, high
                                                                                                      // priority
        );

        Pair<List<TaskInput>, Boolean> result = scheduleDay.topoOrder(tasks);

        assertTrue(result.getSecond());
        List<TaskInput> ordered = result.getFirst();

        // Task 2 should be first (earliest deadline)
        assertEquals(2L, ordered.get(0).getTaskId());
        // Among task 1 and 3 (same deadline), task 3 has higher priority
        assertEquals(3L, ordered.get(1).getTaskId());
        assertEquals(1L, ordered.get(2).getTaskId());
    }

    // ============ Greedy Scheduling Tests ============

    @Test
    @DisplayName("ScheduleDay: Single task in single window")
    void testScheduleDay_SingleTaskSingleWindow() {
        TaskInput task = TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(0.8)
                .deadlineMs(testDateMs + 86400000L)
                .build();

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540) // 9:00 AM
                .endMin(720) // 12:00 PM
                .build();

        PlanResult result = scheduleDay.scheduleDay(
                List.of(task),
                List.of(window),
                defaultWeights,
                defaultParams);

        assertEquals(1, result.getAssignments().size(), "Should schedule 1 task");
        assertEquals(0, result.getUnScheduled().size(), "No unscheduled tasks");

        Assignment assignment = result.getAssignments().get(0);
        assertEquals(1L, assignment.getTaskId());
        assertEquals(testDateMs, assignment.getDateMs());
        assertEquals(540, assignment.getStartMin());
        assertEquals(600, assignment.getEndMin());
    }

    @Test
    @DisplayName("ScheduleDay: Multiple tasks in multiple windows")
    void testScheduleDay_MultipleTasksMultipleWindows() {
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(0.9).build(),
                TaskInput.builder().taskId(2L).durationMin(90).priorityScore(0.7).build(),
                TaskInput.builder().taskId(3L).durationMin(45).priorityScore(0.8).build());

        List<Window> windows = Arrays.asList(
                Window.builder().dateMs(testDateMs).startMin(540).endMin(720).build(), // 9-12
                Window.builder().dateMs(testDateMs).startMin(780).endMin(1020).build() // 13-17
        );

        PlanResult result = scheduleDay.scheduleDay(tasks, windows, defaultWeights, defaultParams);

        assertEquals(3, result.getAssignments().size(), "Should schedule all 3 tasks");
        assertEquals(0, result.getUnScheduled().size());

        // Verify no overlaps
        assertFalse(IntervalTree.hasAnyOverlap(result.getAssignments()), "No overlaps");
    }

    @Test
    @DisplayName("ScheduleDay: Task too long for any window")
    void testScheduleDay_TaskTooLong() {
        TaskInput task = TaskInput.builder()
                .taskId(1L)
                .durationMin(300) // 5 hours
                .priorityScore(0.8)
                .build();

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(720) // Only 3 hours available
                .build();

        PlanResult result = scheduleDay.scheduleDay(
                List.of(task),
                List.of(window),
                defaultWeights,
                defaultParams);

        assertEquals(0, result.getAssignments().size(), "Should not schedule");
        assertEquals(1, result.getUnScheduled().size(), "Should be unscheduled");
        assertEquals(1L, result.getUnScheduled().get(0).getTaskId());
    }

    @Test
    @DisplayName("ScheduleDay: Respect dependencies in scheduling")
    void testScheduleDay_RespectDependencies() {
        // Task 2 depends on Task 1
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(0.5).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.9).dependentTaskIds(List.of(1L))
                        .build());

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(780) // 4 hours available
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, List.of(window), defaultWeights, defaultParams);

        assertEquals(2, result.getAssignments().size());

        Assignment task1Assignment = findAssignment(result.getAssignments(), 1L);
        Assignment task2Assignment = findAssignment(result.getAssignments(), 2L);

        assertNotNull(task1Assignment);
        assertNotNull(task2Assignment);

        // Task 1 must finish before Task 2 starts
        assertTrue(task1Assignment.getEndMin() <= task2Assignment.getStartMin(),
                "Task 1 must complete before Task 2 starts");
    }

    @Test
    @DisplayName("ScheduleDay: Circular dependency returns all unscheduled")
    void testScheduleDay_CircularDependency_AllUnscheduled() {
        // Task 1 -> Task 2 -> Task 1 (cycle)
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).dependentTaskIds(List.of(2L)).build(),
                TaskInput.builder().taskId(2L).durationMin(60).dependentTaskIds(List.of(1L)).build());

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(1020)
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, List.of(window), defaultWeights, defaultParams);

        assertEquals(0, result.getAssignments().size(), "No tasks should be scheduled");
        assertEquals(2, result.getUnScheduled().size(), "All tasks unscheduled due to cycle");

        result.getUnScheduled().forEach(uns -> assertEquals("dependency cycle", uns.getReason()));
    }

    @Test
    @DisplayName("ScheduleDay: Dependent task unscheduled if dependency fails")
    void testScheduleDay_DependencyFails_ChildUnscheduled() {
        // Task 2 depends on Task 1, but Task 1 is too long to fit
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(300).priorityScore(0.5).build(), // Too long
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.9).dependentTaskIds(List.of(1L))
                        .build());

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(720) // Only 3 hours
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, List.of(window), defaultWeights, defaultParams);

        assertEquals(0, result.getAssignments().size());
        assertEquals(2, result.getUnScheduled().size());

        // Task 2 should be unscheduled because dependency (Task 1) is unscheduled
        UnScheduleReason task2Reason = result.getUnScheduled().stream()
                .filter(uns -> uns.getTaskId().equals(2L))
                .findFirst()
                .orElse(null);

        assertNotNull(task2Reason);
        assertEquals("dependency unscheduled", task2Reason.getReason());
    }

    // ============ Local Search Tests ============

    @Test
    @DisplayName("LocalSearch: Improves solution by shifting tasks")
    void testLocalSearch_ImprovesScore() {
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(0.5).deadlineMs(testDateMs + 7200000L)
                        .build(), // 2hr deadline
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.9).deadlineMs(testDateMs + 3600000L)
                        .build() // 1hr deadline (urgent)
        );

        List<Window> windows = List.of(
                Window.builder().dateMs(testDateMs).startMin(0).endMin(240).build() // 4 hours available
        );

        // With local search budget
        Params withLS = Params.builder()
                .slotMin(15)
                .timeBudgetLS(Duration.ofMillis(200))
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, windows, defaultWeights, withLS);

        assertEquals(2, result.getAssignments().size());

        // Task 2 (urgent) should ideally be scheduled earlier
        Assignment task1 = findAssignment(result.getAssignments(), 1L);
        Assignment task2 = findAssignment(result.getAssignments(), 2L);

        assertNotNull(task1);
        assertNotNull(task2);

        // Verify total utility is positive
        double totalUtility = result.getAssignments().stream()
                .mapToDouble(a -> a.getUtility() != null ? a.getUtility() : 0.0)
                .sum();

        assertTrue(totalUtility > 0, "Total utility should be positive");
    }

    @Test
    @DisplayName("LocalSearch: No improvement if budget is zero")
    void testLocalSearch_ZeroBudget_NoChange() {
        TaskInput task = TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(0.8)
                .build();

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(720)
                .build();

        Params noBudget = Params.builder()
                .slotMin(15)
                .timeBudgetLS(Duration.ZERO)
                .build();

        PlanResult result = scheduleDay.scheduleDay(List.of(task), List.of(window), defaultWeights, noBudget);

        assertEquals(1, result.getAssignments().size());
        // Initial greedy placement
        assertEquals(540, result.getAssignments().get(0).getStartMin());
    }

    @Test
    @DisplayName("LocalSearch: Does not violate dependencies")
    void testLocalSearch_PreservesDependencies() {
        // Task 2 depends on Task 1
        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(0.5).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.9).dependentTaskIds(List.of(1L))
                        .build());

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(0)
                .endMin(300) // Large window for local search to explore
                .build();

        Params withLS = Params.builder()
                .slotMin(15)
                .timeBudgetLS(Duration.ofMillis(500))
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, List.of(window), defaultWeights, withLS);

        assertEquals(2, result.getAssignments().size());

        Assignment task1 = findAssignment(result.getAssignments(), 1L);
        Assignment task2 = findAssignment(result.getAssignments(), 2L);

        // Task 1 must still complete before Task 2 starts after local search
        assertTrue(task1.getEndMin() <= task2.getStartMin(),
                "Dependencies must be preserved after local search");
    }

    // ============ Edge Cases ============

    @Test
    @DisplayName("EdgeCase: Empty task list")
    void testScheduleDay_EmptyTasks() {
        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(720)
                .build();

        PlanResult result = scheduleDay.scheduleDay(List.of(), List.of(window), defaultWeights, defaultParams);

        assertEquals(0, result.getAssignments().size());
        assertEquals(0, result.getUnScheduled().size());
    }

    @Test
    @DisplayName("EdgeCase: Empty window list")
    void testScheduleDay_EmptyWindows() {
        TaskInput task = TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(0.8)
                .build();

        PlanResult result = scheduleDay.scheduleDay(List.of(task), List.of(), defaultWeights, defaultParams);

        assertEquals(0, result.getAssignments().size());
        assertEquals(1, result.getUnScheduled().size());
    }

    @Test
    @DisplayName("EdgeCase: Task with null duration defaults to 0")
    void testScheduleDay_NullDuration() {
        TaskInput task = TaskInput.builder()
                .taskId(1L)
                .durationMin(null)
                .priorityScore(0.8)
                .build();

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(540)
                .endMin(720)
                .build();

        PlanResult result = scheduleDay.scheduleDay(List.of(task), List.of(window), defaultWeights, defaultParams);

        // Task with 0 duration should be scheduled
        assertEquals(1, result.getAssignments().size());
        Assignment assignment = result.getAssignments().get(0);
        assertEquals(assignment.getStartMin(), assignment.getEndMin(), "Duration should be 0");
    }

    @Test
    @DisplayName("EdgeCase: High priority task scheduled first despite later in list")
    void testScheduleDay_PriorityOrdering() {
        Long now = System.currentTimeMillis();

        List<TaskInput> tasks = Arrays.asList(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(0.5).deadlineMs(now + 86400000L).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.9).deadlineMs(now + 7200000L).build(), // More
                                                                                                                      // urgent
                TaskInput.builder().taskId(3L).durationMin(60).priorityScore(0.3).deadlineMs(now + 86400000L).build());

        Window window = Window.builder()
                .dateMs(testDateMs)
                .startMin(0)
                .endMin(240)
                .build();

        PlanResult result = scheduleDay.scheduleDay(tasks, List.of(window), defaultWeights, defaultParams);

        assertEquals(3, result.getAssignments().size());

        // Task 2 (urgent deadline) should be scheduled first
        Assignment first = result.getAssignments().stream()
                .min((a, b) -> Integer.compare(a.getStartMin(), b.getStartMin()))
                .orElse(null);

        assertNotNull(first);
        assertEquals(2L, first.getTaskId(), "Most urgent task should be scheduled first");
    }

    // ============ Helper Methods ============

    private int findTaskIndex(List<TaskInput> tasks, Long taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTaskId().equals(taskId)) {
                return i;
            }
        }
        return -1;
    }

    private Assignment findAssignment(List<Assignment> assignments, Long taskId) {
        return assignments.stream()
                .filter(a -> a.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
    }
}
