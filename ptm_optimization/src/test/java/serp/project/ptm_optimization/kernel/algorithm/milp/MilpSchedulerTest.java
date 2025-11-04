/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.kernel.algorithm.milp;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UnScheduleReason;

public class MilpSchedulerTest {

    @Test
    void simpleMilpProducesPlan_respectsCapacityAndPrecedence() {
        assumeTrue(MilpScheduler.isAvailable(), "OR-Tools unavailable; skipping MILP test");
        MilpScheduler ms = new MilpScheduler();

        long dateMs = Instant.parse("2025-11-05T00:00:00Z").toEpochMilli();
        List<Window> wins = List.of(
                Window.builder().dateMs(dateMs).startMin(9 * 60).endMin(12 * 60).build(),
                Window.builder().dateMs(dateMs).startMin(13 * 60).endMin(17 * 60).build());

        TaskInput t1 = TaskInput.builder().taskId(1L).durationMin(60).priorityScore(1.0).build();
        TaskInput t2 = TaskInput.builder().taskId(2L).durationMin(120).priorityScore(0.8).dependentTaskIds(List.of(1L))
                .build();
        TaskInput t3 = TaskInput.builder().taskId(3L).durationMin(90).priorityScore(0.6).build();
        var tasks = List.of(t1, t2, t3);

        Weights w = Weights.builder().wPriority(1.0).wDeadline(0.0).build();

        PlanResult res = ms.schedule(tasks, wins, w, 15);
        assertNotNull(res);
        assertTrue(res.getAssignments().size() >= 2);
        // ensure t2 starts after t1 ends
        var a1 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(1L)).findFirst().orElse(null);
        var a2 = res.getAssignments().stream().filter(a -> a.getTaskId().equals(2L)).findFirst().orElse(null);
        if (a1 != null && a2 != null && a1.getDateMs().equals(a2.getDateMs())) {
            assertTrue(a1.getEndMin() <= a2.getStartMin());
        }
        // no overlaps on same day
        assertFalse(anyOverlap(res));
    }

    @Test
    void oversizedTask_unscheduled_and_cycle_unschedulesBoth() {
        assumeTrue(MilpScheduler.isAvailable(), "OR-Tools unavailable; skipping MILP test");
        MilpScheduler ms = new MilpScheduler();

        long day = Instant.parse("2025-11-06T00:00:00Z").toEpochMilli();
        List<Window> wins = List.of(
                Window.builder().dateMs(day).startMin(9 * 60).endMin(12 * 60).build(),
                Window.builder().dateMs(day).startMin(13 * 60).endMin(17 * 60).build());

        TaskInput big = TaskInput.builder().taskId(10L).durationMin(600).priorityScore(0.9).build(); // 10h too big
        TaskInput a = TaskInput.builder().taskId(11L).durationMin(60).priorityScore(0.8).dependentTaskIds(List.of(12L))
                .build();
        TaskInput b = TaskInput.builder().taskId(12L).durationMin(60).priorityScore(0.7).dependentTaskIds(List.of(11L))
                .build();

        Weights w = Weights.builder().wPriority(1.0).wDeadline(0.0).build();
        PlanResult res = ms.schedule(List.of(big, a, b), wins, w, 15);
        assertNotNull(res);
        var unsIds = res.getUnScheduled().stream().map(UnScheduleReason::getTaskId).toList();
        assertTrue(unsIds.contains(10L), "Oversized task should be unscheduled");
        // cycle prevents scheduling either
        assertTrue(unsIds.contains(11L) && unsIds.contains(12L), "Cyclic deps should be unscheduled");
    }

    // helpers
    private static boolean anyOverlap(PlanResult res) {
        var list = res.getAssignments();
        var byDate = new java.util.HashMap<Long, java.util.List<serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment>>();
        for (var a : list)
            byDate.computeIfAbsent(a.getDateMs(), k -> new java.util.ArrayList<>()).add(a);
        for (var e : byDate.entrySet()) {
            var L = e.getValue();
            L.sort(java.util.Comparator.comparingInt(
                    serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment::getStartMin));
            for (int i = 1; i < L.size(); i++)
                if (L.get(i - 1).getEndMin() > L.get(i).getStartMin())
                    return true;
        }
        return false;
    }
}
