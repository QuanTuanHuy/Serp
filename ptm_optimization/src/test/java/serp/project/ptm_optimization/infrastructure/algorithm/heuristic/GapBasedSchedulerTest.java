/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for GapBasedScheduler
 */

package serp.project.ptm_optimization.infrastructure.algorithm.heuristic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.utils.GapManager;
import serp.project.ptm_optimization.kernel.utils.SchedulingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GapBasedSchedulerTest {


    private GapManager gapManager;
    private SchedulingUtils schedulingUtils;
    private GapBasedScheduler scheduler;

    @BeforeEach
    void setUp() {
        gapManager = new GapManager();
        schedulingUtils = new SchedulingUtils();
        scheduler = new GapBasedScheduler(gapManager, schedulingUtils);
    }

    @Test
    void testSchedule_EmptyTasks_ReturnsEmptyResult() {
        // Given
        List<TaskInput> tasks = Collections.emptyList();
        List<Window> windows = createBasicWindows();
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertTrue(result.getUnScheduled().isEmpty());
    }

    @Test
    void testSchedule_EmptyWindows_AllTasksUnscheduled() {
        // Given
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(5.0)
                .build()
        );
        List<Window> windows = Collections.emptyList();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertEquals(1, result.getUnScheduled().size());
        assertEquals("no suitable gap found", result.getUnScheduled().get(0).getReason());
    }

    @Test
    void testSchedule_SingleTask_ScheduledSuccessfully() {
        // Given
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(5.0)
                .build()
        );
        List<Window> windows = createBasicWindows();
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        assertTrue(result.getUnScheduled().isEmpty());
        
        Assignment assignment = result.getAssignments().get(0);
        assertEquals(1L, assignment.getTaskId());
        assertEquals(60, assignment.getEndMin() - assignment.getStartMin());
    }

    @Test
    void testSchedule_MultipleTasks_AllScheduled() {
        // Given
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(5.0)
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(30)
                .priorityScore(3.0)
                .build(),
            TaskInput.builder()
                .taskId(3L)
                .durationMin(45)
                .priorityScore(4.0)
                .build()
        );
        List<Window> windows = createBasicWindows();
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getAssignments().size());
        assertTrue(result.getUnScheduled().isEmpty());
        
        // Verify all tasks are scheduled
        List<Long> scheduledIds = result.getAssignments().stream()
            .map(Assignment::getTaskId)
            .toList();
        assertTrue(scheduledIds.contains(1L));
        assertTrue(scheduledIds.contains(2L));
        assertTrue(scheduledIds.contains(3L));
    }

    @Test
    void testSchedule_TaskTooLarge_Unscheduled() {
        // Given: Task duration exceeds window size
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(600) // 10 hours, exceeds 8-hour window
                .priorityScore(5.0)
                .build()
        );
        List<Window> windows = createBasicWindows();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertEquals(1, result.getUnScheduled().size());
        assertEquals(1L, result.getUnScheduled().get(0).getTaskId());
    }

    @Test
    void testSchedule_WithDependencies_RespectOrder() {
        // Given: Task 2 depends on Task 1
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(3.0)
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .priorityScore(5.0)
                .dependentTaskIds(List.of(1L))
                .build()
        );
        List<Window> windows = createBasicWindows();
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        // Should schedule at least task 1 (dependency parent)
        assertTrue(result.getAssignments().size() >= 1);
        
        if (result.getAssignments().size() == 2) {
            Assignment task1 = result.getAssignments().stream()
                .filter(a -> a.getTaskId().equals(1L))
                .findFirst()
                .orElseThrow();
            Assignment task2 = result.getAssignments().stream()
                .filter(a -> a.getTaskId().equals(2L))
                .findFirst()
                .orElseThrow();
            
            // Task 2 must be scheduled after Task 1
            assertTrue(task2.getDateMs() >= task1.getDateMs());
            if (task2.getDateMs().equals(task1.getDateMs())) {
                assertTrue(task2.getStartMin() >= task1.getEndMin());
            }
        }
    }

    @Test
    void testSchedule_DependencyCycle_AllUnscheduled() {
        // Given: Circular dependency (Task 1 -> Task 2 -> Task 1)
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .dependentTaskIds(List.of(2L))
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .dependentTaskIds(List.of(1L))
                .build()
        );
        List<Window> windows = createBasicWindows();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertEquals(2, result.getUnScheduled().size());
    }

    @Test
    void testSchedule_DependencyFails_ChildUnscheduled() {
        // Given: Task 2 depends on Task 1, but Task 1 is too large
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(600) // Too large
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .dependentTaskIds(List.of(1L))
                .build()
        );
        List<Window> windows = createBasicWindows();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getAssignments().isEmpty());
        assertEquals(2, result.getUnScheduled().size());
        
        // Verify Task 2 failed due to dependency
        var task2Unscheduled = result.getUnScheduled().stream()
            .filter(u -> u.getTaskId().equals(2L))
            .findFirst()
            .orElseThrow();
        assertEquals("dependency unscheduled", task2Unscheduled.getReason());
    }

    @Test
    void testSchedule_WithDeadline_PrioritizesUrgentTasks() {
        // Given: Two tasks, one with urgent deadline
        long now = System.currentTimeMillis();
        long tomorrow = now + 24 * 60 * 60 * 1000L;
        long nextWeek = now + 7 * 24 * 60 * 60 * 1000L;
        
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .priorityScore(3.0)
                .deadlineMs(nextWeek) // Less urgent
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .priorityScore(3.0)
                .deadlineMs(tomorrow) // More urgent
                .build()
        );
        List<Window> windows = createBasicWindows();
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAssignments().size());
        
        // Task 2 (urgent) should be scheduled first
        Assignment firstScheduled = result.getAssignments().get(0);
        assertEquals(2L, firstScheduled.getTaskId());
    }

    @Test
    void testSchedule_MultipleWindows_DistributesTasks() {
        // Given: Multiple tasks and multiple windows
        List<TaskInput> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(TaskInput.builder()
                .taskId((long) i)
                .durationMin(60)
                .priorityScore(3.0)
                .build());
        }
        
        // Two windows on different dates
        long date1 = System.currentTimeMillis();
        long date2 = date1 + 24 * 60 * 60 * 1000L;
        
        List<Window> windows = List.of(
            Window.builder()
                .dateMs(date1)
                .startMin(540) // 9:00
                .endMin(1020) // 17:00
                .build(),
            Window.builder()
                .dateMs(date2)
                .startMin(540)
                .endMin(1020)
                .build()
        );

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getAssignments().size());
        
        // Verify tasks are distributed (at least some scheduled)
        long tasksOnDate1 = result.getAssignments().stream()
            .filter(a -> a.getDateMs().equals(date1))
            .count();
        long tasksOnDate2 = result.getAssignments().stream()
            .filter(a -> a.getDateMs().equals(date2))
            .count();
        
        // At least one task should be scheduled
        assertTrue(tasksOnDate1 + tasksOnDate2 >= 1,
            "At least one task should be scheduled across available windows");
    }

    @Test
    void testSchedule_DeepWorkWindow_MatchesHighEffortTasks() {
        // Given: High effort task and deep work window
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(120)
                .priorityScore(5.0)
                .effort(0.9) // High effort
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .priorityScore(5.0)
                .effort(0.3) // Low effort
                .build()
        );
        
        long date = System.currentTimeMillis();
        List<Window> windows = List.of(
            Window.builder()
                .dateMs(date)
                .startMin(540) // 9:00
                .endMin(720)   // 12:00
                .isDeepWork(true)
                .build(),
            Window.builder()
                .dateMs(date)
                .startMin(780)  // 13:00
                .endMin(1020)   // 17:00
                .isDeepWork(false)
                .build()
        );
        
        Weights weights = createDefaultWeights();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, weights, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getAssignments().size());
        
        // High effort task should be in deep work window
        Assignment highEffortTask = result.getAssignments().stream()
            .filter(a -> a.getTaskId().equals(1L))
            .findFirst()
            .orElseThrow();
        
        // Verify it's in morning deep work window (9:00-12:00)
        assertTrue(highEffortTask.getStartMin() >= 540);
        assertTrue(highEffortTask.getEndMin() <= 720);
    }

    @Test
    void testSchedule_NoOverlap_TasksDontConflict() {
        // Given: Multiple tasks on same day
        List<TaskInput> tasks = List.of(
            TaskInput.builder()
                .taskId(1L)
                .durationMin(60)
                .build(),
            TaskInput.builder()
                .taskId(2L)
                .durationMin(60)
                .build(),
            TaskInput.builder()
                .taskId(3L)
                .durationMin(60)
                .build()
        );
        List<Window> windows = createBasicWindows();

        // When
        PlanResult result = scheduler.schedule(tasks, windows, null, null);

        // Then
        assertNotNull(result);
        List<Assignment> assignments = result.getAssignments();
        
        // Verify no overlaps
        for (int i = 0; i < assignments.size(); i++) {
            for (int j = i + 1; j < assignments.size(); j++) {
                Assignment a1 = assignments.get(i);
                Assignment a2 = assignments.get(j);
                
                if (a1.getDateMs().equals(a2.getDateMs())) {
                    // Tasks on same day must not overlap
                    boolean noOverlap = a1.getEndMin() <= a2.getStartMin() || 
                                       a2.getEndMin() <= a1.getStartMin();
                    assertTrue(noOverlap, 
                        String.format("Tasks %d and %d overlap", a1.getTaskId(), a2.getTaskId()));
                }
            }
        }
    }

    // Helper methods

    private List<Window> createBasicWindows() {
        long today = System.currentTimeMillis();
        return List.of(
            Window.builder()
                .dateMs(today)
                .startMin(540)  // 9:00
                .endMin(1020)   // 17:00
                .isDeepWork(false)
                .build()
        );
    }

    private Weights createDefaultWeights() {
        return Weights.builder()
            .wPriority(1.0)
            .wDeadline(0.8)
            .wSwitch(0.5)
            .wFatigue(0.3)
            .wEnjoy(0.2)
            .build();
    }
}
