/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.kernel.algorithm.heuristic;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Params;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.utils.UtilityModel;

public class ScheduleDayTest {

    @Test
    void simpleGreedyAndLocalSearchProducesPlan() {
        UtilityModel util = new UtilityModel();
        ScheduleDay sd = new ScheduleDay(util);

        long dateMs = Instant.parse("2024-11-01T00:00:00Z").toEpochMilli();
        List<Window> wins = List.of(
                Window.builder().dateMs(dateMs).startMin(9 * 60).endMin(12 * 60).build(),
                Window.builder().dateMs(dateMs).startMin(13 * 60).endMin(17 * 60).build());

        Long dl = Instant.parse("2024-11-01T16:00:00Z").toEpochMilli();
        TaskInput t1 = TaskInput.builder().taskId(1L).durationMin(90).priorityScore(0.9).deadlineMs(dl).build();
        TaskInput t2 = TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.6).build();
        TaskInput t3 = TaskInput.builder().taskId(3L).durationMin(120).priorityScore(0.8).dependentTaskIds(List.of(1L))
                .build();
        List<TaskInput> tasks = List.of(t1, t2, t3);

        Weights w = Weights.builder().wPriority(1.0).wDeadline(0.01).wSwitch(0.2).wFatigue(0.001).wEnjoy(0.1).build();
        Params p = Params.builder().slotMin(15).timeBudgetLS(Duration.ofMillis(100)).build();

        PlanResult res = sd.scheduleDay(tasks, wins, w, p);
        assertNotNull(res);
        assertNotNull(res.getAssignments());
        assertTrue(res.getAssignments().size() >= 2);
        assertNotNull(res.getUnScheduled());
    }

    @Test
    void complexSchedulingAcrossDays_respectsPrecedence_noOverlap_and_unschedulesTooLargeTask() {
        UtilityModel util = new UtilityModel();
        ScheduleDay sd = new ScheduleDay(util);

        long day1 = Instant.parse("2025-11-05T00:00:00Z").toEpochMilli();
        long day2 = Instant.parse("2025-11-06T00:00:00Z").toEpochMilli();
        List<Window> wins = List.of(
                // day 1 windows
                Window.builder().dateMs(day1).startMin(9 * 60).endMin(12 * 60).build(),
                Window.builder().dateMs(day1).startMin(13 * 60).endMin(17 * 60).build(),
                // day 2 windows
                Window.builder().dateMs(day2).startMin(9 * 60).endMin(12 * 60).build(),
                Window.builder().dateMs(day2).startMin(13 * 60).endMin(17 * 60).build());

        Long dl1 = Instant.parse("2025-11-05T16:00:00Z").toEpochMilli();
        Long dl5 = Instant.parse("2025-11-06T10:00:00Z").toEpochMilli();
        // Tasks: chain and branching deps; one too large for any window
        TaskInput t1 = TaskInput.builder().taskId(1L).durationMin(120).priorityScore(0.9).deadlineMs(dl1).build();
        TaskInput t2 = TaskInput.builder().taskId(2L).durationMin(60).priorityScore(0.5).build();
        TaskInput t3 = TaskInput.builder().taskId(3L).durationMin(180).priorityScore(0.8).dependentTaskIds(List.of(1L))
                .build();
        TaskInput t4 = TaskInput.builder().taskId(4L).durationMin(300).priorityScore(0.7)
                .dependentTaskIds(List.of(1L, 2L)).build(); // too large (5h)
        TaskInput t5 = TaskInput.builder().taskId(5L).durationMin(90).priorityScore(0.95).deadlineMs(dl5).build();
        TaskInput t6 = TaskInput.builder().taskId(6L).durationMin(30).priorityScore(0.4).enjoyability(0.6).build();
        TaskInput t7 = TaskInput.builder().taskId(7L).durationMin(30).priorityScore(0.4).enjoyability(-0.6).build();
        List<TaskInput> tasks = List.of(t1, t2, t3, t4, t5, t6, t7);

        Weights w = Weights.builder().wPriority(1.0).wDeadline(0.5).wSwitch(0.2).wFatigue(0.1).wEnjoy(0.1).build();
        Params p = Params.builder().slotMin(15).timeBudgetLS(Duration.ofMillis(250)).build();

        var res = sd.scheduleDay(tasks, wins, w, p);
        assertNotNull(res);
        assertNotNull(res.getAssignments());
        assertNotNull(res.getUnScheduled());

        // Expect t4 unscheduled due to size, others placed
        var unsIds = res.getUnScheduled().stream().map(UnScheduleReason::getTaskId).toList();
        assertTrue(unsIds.contains(4L), "Task 4 should be unscheduled as it's too large for any window");
        assertTrue(res.getAssignments().size() >= 6 - 1); // at least six tasks minus one unscheduled

        // No overlaps on the same day
        assertFalse(anyOverlap(res.getAssignments()), "Assignments should not overlap on same date");

        // Precedence: for each edge, ensure end(dep) <= start(child) on same day, or
        // dep day < child day
        MapIdx idx = indexByTask(res.getAssignments());
        assertPrecedence(idx, res.getAssignments(), 3L, 1L);
        assertPrecedence(idx, res.getAssignments(), 4L, 1L); // although 4L unscheduled, assertion tolerates missing
        assertPrecedence(idx, res.getAssignments(), 4L, 2L);
    }

    @Test
    void cyclicDependency_unschedulesAll() {
        UtilityModel util = new UtilityModel();
        ScheduleDay sd = new ScheduleDay(util);

        long day = Instant.parse("2025-11-05T00:00:00Z").toEpochMilli();
        List<Window> wins = List.of(Window.builder().dateMs(day).startMin(9 * 60).endMin(17 * 60).build());
        TaskInput a = TaskInput.builder().taskId(101L).durationMin(60).priorityScore(0.8)
                .dependentTaskIds(List.of(102L)).build();
        TaskInput b = TaskInput.builder().taskId(102L).durationMin(60).priorityScore(0.7)
                .dependentTaskIds(List.of(101L)).build();
        var res = sd.scheduleDay(List.of(a, b), wins, Weights.builder().wPriority(1.0).wDeadline(0.5).build(),
                Params.builder().slotMin(15).timeBudgetLS(Duration.ofMillis(100)).build());
        assertTrue(res.getAssignments().isEmpty());
        assertEquals(2, res.getUnScheduled().size());
        assertTrue(res.getUnScheduled().stream().allMatch(u -> "dependency cycle".equals(u.getReason())));
    }

    // ---------- helpers ----------
    private static boolean anyOverlap(
            List<serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment> as) {
        // group by date, then check intervals
        var byDate = new java.util.HashMap<Long, List<serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment>>();
        for (var a : as) {
            byDate.computeIfAbsent(a.getDateMs(), k -> new ArrayList<>()).add(a);
        }
        for (var entry : byDate.entrySet()) {
            var list = entry.getValue();
            list.sort(java.util.Comparator.comparingInt(
                    serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment::getStartMin));
            for (int i = 1; i < list.size(); i++) {
                var prev = list.get(i - 1);
                var cur = list.get(i);
                if (prev.getEndMin() > cur.getStartMin())
                    return true;
            }
        }
        return false;
    }

    private static class MapIdx {
        final java.util.Map<Long, serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment> byTask = new java.util.HashMap<>();
    }

    private static MapIdx indexByTask(
            List<serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment> as) {
        MapIdx mi = new MapIdx();
        for (var a : as)
            mi.byTask.put(a.getTaskId(), a);
        return mi;
    }

    private static void assertPrecedence(MapIdx idx,
            List<serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment> as, Long child,
            Long dep) {
        var aChild = idx.byTask.get(child);
        var aDep = idx.byTask.get(dep);
        // if either missing (e.g., unscheduled), skip assertion
        if (aChild == null || aDep == null)
            return;
        if (aDep.getDateMs().equals(aChild.getDateMs())) {
            assertTrue(aDep.getEndMin() <= aChild.getStartMin(), "dep must finish before child starts on same day");
        } else {
            assertTrue(aDep.getDateMs() <= aChild.getDateMs(), "dep date must be <= child date");
        }
    }
}
