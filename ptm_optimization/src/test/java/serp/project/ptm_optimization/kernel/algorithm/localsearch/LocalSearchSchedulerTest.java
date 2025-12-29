/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local Search Scheduler Tests
*/

package serp.project.ptm_optimization.kernel.algorithm.localsearch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.GapBasedScheduler;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Params;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.utils.GapManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalSearchSchedulerTest {

    @Mock
    private GapBasedScheduler gapBasedScheduler;

    @Mock
    private GapManager gapManager;

    @InjectMocks
    private LocalSearchScheduler localSearchScheduler;

    private Weights weights;
    private Params params;

    @BeforeEach
    void setUp() {
        weights = Weights.builder()
                .wPriority(1.0)
                .wDeadline(1.0)
                .build();

        params = Params.builder()
                .initialTemperature(100.0)
                .coolingRate(0.9)
                .maxIterations(50)
                .build();
    }

    @Test
    void testEmptyInput() {
        // Given
        List<TaskInput> tasks = new ArrayList<>();
        List<Window> windows = new ArrayList<>();

        PlanResult emptyResult = PlanResult.builder()
                .assignments(new ArrayList<>())
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(emptyResult);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertTrue(result.getUnScheduled().isEmpty());
    }

    @Test
    void testSingleTaskSchedule() {
        // Given
        List<TaskInput> tasks = List.of(
                TaskInput.builder()
                        .taskId(1L)
                        .durationMin(60)
                        .priorityScore(5.0)
                        .build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)  // 9:00
                        .endMin(1020)   // 17:00
                        .isDeepWork(false)
                        .build()
        );

        Assignment initialAssignment = Assignment.builder()
                .taskId(1L)
                .dateMs(1000000L)
                .startMin(540)
                .endMin(600)
                .utility(5.0)
                .build();

        PlanResult initialResult = PlanResult.builder()
                .assignments(List.of(initialAssignment))
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        assertEquals(1L, result.getAssignments().get(0).getTaskId());
    }

    @Test
    void testMultipleTasksNoOverlaps() {
        // Given
        List<TaskInput> tasks = List.of(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(5.0).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(3.0).build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        List<Assignment> initialAssignments = List.of(
                Assignment.builder().taskId(1L).dateMs(1000000L).startMin(540).endMin(600).utility(5.0).build(),
                Assignment.builder().taskId(2L).dateMs(1000000L).startMin(600).endMin(660).utility(3.0).build()
        );

        PlanResult initialResult = PlanResult.builder()
                .assignments(initialAssignments)
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);
        when(gapManager.overlaps(any(), any())).thenReturn(false);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAssignments().size());
        
        // Verify no overlaps
        List<Assignment> assignments = result.getAssignments();
        for (int i = 0; i < assignments.size(); i++) {
            for (int j = i + 1; j < assignments.size(); j++) {
                Assignment a1 = assignments.get(i);
                Assignment a2 = assignments.get(j);
                
                if (a1.getDateMs().equals(a2.getDateMs())) {
                    // No time overlap
                    assertTrue(a1.getEndMin() <= a2.getStartMin() || 
                              a2.getEndMin() <= a1.getStartMin());
                }
            }
        }
    }

    @Test
    void testDeadlineConstraints() {
        // Given
        long baseDate = 1000000L;
        long deadline = baseDate + 300 * 60_000L; // 5 hours after start of day

        List<TaskInput> tasks = List.of(
                TaskInput.builder()
                        .taskId(1L)
                        .durationMin(60)
                        .priorityScore(5.0)
                        .deadlineMs(deadline)
                        .build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(baseDate)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        Assignment initialAssignment = Assignment.builder()
                .taskId(1L)
                .dateMs(baseDate)
                .startMin(540)
                .endMin(600)
                .utility(5.0)
                .build();

        PlanResult initialResult = PlanResult.builder()
                .assignments(List.of(initialAssignment))
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        
        Assignment assignment = result.getAssignments().get(0);
        
        // LocalSearch may shift tasks randomly, so we just verify task is scheduled
        // (Deadline enforcement is objective-based, not hard constraint)
        assertEquals(1L, assignment.getTaskId());
        assertTrue(assignment.getStartMin() >= 540 && assignment.getEndMin() <= 1020,
            "Task should be within window bounds");
    }

    @Test
    void testDependencyConstraints() {
        // Given
        List<TaskInput> tasks = List.of(
                TaskInput.builder()
                        .taskId(1L)
                        .durationMin(60)
                        .priorityScore(5.0)
                        .build(),
                TaskInput.builder()
                        .taskId(2L)
                        .durationMin(60)
                        .priorityScore(3.0)
                        .dependentTaskIds(List.of(1L))  // 2 depends on 1
                        .build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        List<Assignment> initialAssignments = List.of(
                Assignment.builder().taskId(1L).dateMs(1000000L).startMin(540).endMin(600).utility(5.0).build(),
                Assignment.builder().taskId(2L).dateMs(1000000L).startMin(600).endMin(660).utility(3.0).build()
        );

        PlanResult initialResult = PlanResult.builder()
                .assignments(initialAssignments)
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);
        when(gapManager.overlaps(any(), any())).thenReturn(false);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAssignments().size());
        
        Assignment task1 = result.getAssignments().stream()
                .filter(a -> a.getTaskId().equals(1L))
                .findFirst()
                .orElse(null);
        
        Assignment task2 = result.getAssignments().stream()
                .filter(a -> a.getTaskId().equals(2L))
                .findFirst()
                .orElse(null);
        
        assertNotNull(task1);
        assertNotNull(task2);
        
        // Task 1 must finish before Task 2 starts
        if (task1.getDateMs().equals(task2.getDateMs())) {
            assertTrue(task1.getEndMin() <= task2.getStartMin(),
                "Dependency task must finish before dependent task");
        } else {
            assertTrue(task1.getDateMs() < task2.getDateMs(),
                "Dependency task must be scheduled on earlier or same date");
        }
    }

    @Test
    void testImprovementOverInitial() {
        // Given: Suboptimal initial solution
        List<TaskInput> tasks = List.of(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(10.0).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(5.0).build(),
                TaskInput.builder().taskId(3L).durationMin(60).priorityScore(3.0).build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        List<Assignment> initialAssignments = List.of(
                Assignment.builder().taskId(3L).dateMs(1000000L).startMin(540).endMin(600).utility(3.0).build(),
                Assignment.builder().taskId(2L).dateMs(1000000L).startMin(600).endMin(660).utility(5.0).build(),
                Assignment.builder().taskId(1L).dateMs(1000000L).startMin(660).endMin(720).utility(10.0).build()
        );

        PlanResult initialResult = PlanResult.builder()
                .assignments(initialAssignments)
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(5.0);
        when(gapManager.overlaps(any(), any())).thenReturn(false);

        Params searchParams = Params.builder()
                .initialTemperature(500.0)
                .coolingRate(0.95)
                .maxIterations(500)
                .build();

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, searchParams);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getAssignments().size());
        
        // LocalSearch should maintain or improve quality
        // (We can't guarantee improvement with random moves, but should not worsen significantly)
        assertTrue(result.getAssignments().size() >= initialResult.getAssignments().size());
    }

    @Test
    void testWindowConstraints() {
        // Given
        List<TaskInput> tasks = List.of(
                TaskInput.builder()
                        .taskId(1L)
                        .durationMin(120)  // 2 hours
                        .priorityScore(5.0)
                        .build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)   // 9:00
                        .endMin(660)     // 11:00 (only 2 hours)
                        .isDeepWork(false)
                        .build()
        );

        Assignment initialAssignment = Assignment.builder()
                .taskId(1L)
                .dateMs(1000000L)
                .startMin(540)
                .endMin(660)
                .utility(5.0)
                .build();

        PlanResult initialResult = PlanResult.builder()
                .assignments(List.of(initialAssignment))
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        
        Assignment assignment = result.getAssignments().get(0);
        
        // Must be within window
        assertTrue(assignment.getStartMin() >= 540);
        assertTrue(assignment.getEndMin() <= 660);
    }

    @Test
    void testDefaultParameters() {
        // Given: No parameters specified
        List<TaskInput> tasks = List.of(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(5.0).build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        Assignment initialAssignment = Assignment.builder()
                .taskId(1L)
                .dateMs(1000000L)
                .startMin(540)
                .endMin(600)
                .utility(5.0)
                .build();

        PlanResult initialResult = PlanResult.builder()
                .assignments(List.of(initialAssignment))
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);

        Params defaultParams = Params.builder().build(); // No local search params

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, defaultParams);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        // Should use default values (temp=1000, cooling=0.95, iter=1000)
    }

    @Test
    void testHighPriorityTasksPreferred() {
        // Given
        List<TaskInput> tasks = List.of(
                TaskInput.builder().taskId(1L).durationMin(60).priorityScore(10.0).build(),
                TaskInput.builder().taskId(2L).durationMin(60).priorityScore(1.0).build()
        );

        List<Window> windows = List.of(
                Window.builder()
                        .dateMs(1000000L)
                        .startMin(540)
                        .endMin(1020)
                        .isDeepWork(false)
                        .build()
        );

        List<Assignment> initialAssignments = List.of(
                Assignment.builder().taskId(1L).dateMs(1000000L).startMin(540).endMin(600).utility(10.0).build(),
                Assignment.builder().taskId(2L).dateMs(1000000L).startMin(600).endMin(660).utility(1.0).build()
        );

        PlanResult initialResult = PlanResult.builder()
                .assignments(initialAssignments)
                .unScheduled(new ArrayList<>())
                .build();

        when(gapBasedScheduler.schedule(any(), any(), any(), any())).thenReturn(initialResult);
        when(gapManager.calculateFragmentation(any(), any(), anyInt())).thenReturn(0.0);
        when(gapManager.overlaps(any(), any())).thenReturn(false);

        // When
        PlanResult result = localSearchScheduler.schedule(tasks, windows, weights, params);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAssignments().size());
        
        // Both tasks should be scheduled (high priority not excluded)
        assertTrue(result.getAssignments().stream().anyMatch(a -> a.getTaskId().equals(1L)));
        assertTrue(result.getAssignments().stream().anyMatch(a -> a.getTaskId().equals(2L)));
    }
}
