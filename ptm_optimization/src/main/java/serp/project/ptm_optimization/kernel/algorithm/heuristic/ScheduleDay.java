/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.kernel.algorithm.heuristic;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Params;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UtilityBreakdown;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.utils.UtilityModel;

@Component
@Slf4j
public class ScheduleDay {

    private final UtilityModel utilityModel;

    public ScheduleDay(UtilityModel utilityModel) {
        this.utilityModel = utilityModel;
    }

    public PlanResult scheduleDay(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        Pair<List<TaskInput>, Boolean> topo = topoOrder(tasks);
        List<TaskInput> ordered = topo.getFirst();
        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> unscheduled = new ArrayList<>();
        if (!topo.getSecond()) {
            for (TaskInput t : tasks) {
                unscheduled.add(UnScheduleReason.builder().taskId(t.getTaskId()).reason("dependency cycle").build());
            }
            return PlanResult.builder().assignments(assignments).unScheduled(unscheduled).build();
        }

        GreedyState gstate = greedySchedule(ordered, windows, weights);
        assignments = gstate.assignments;
        unscheduled.addAll(gstate.unscheduled);

        // Local Search improvement (time-bounded)
        Map<Long, TaskInput> taskMap = tasks.stream().collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
        Map<Long, List<Long>> depMap = new HashMap<>();
        for (TaskInput t : tasks) {
            depMap.put(t.getTaskId(), Optional.ofNullable(t.getDependentTaskIds()).orElse(List.of()));
        }
        Duration budget = Optional.ofNullable(params).map(Params::getTimeBudgetLS).orElse(Duration.ZERO);
        int step = Optional.ofNullable(params).map(Params::getSlotMin).orElse(15);
        assignments = localSearchImprove(assignments, windows, weights, taskMap, depMap, budget, step);

        return PlanResult.builder().assignments(assignments).unScheduled(unscheduled).build();
    }

    public Pair<List<TaskInput>, Boolean> topoOrder(List<TaskInput> tasks) {
        Map<Long, TaskInput> idToTask = tasks.stream().collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
        Map<Long, Integer> indeg = new HashMap<>();
        Map<Long, List<Long>> graph = new HashMap<>();
        for (TaskInput t : tasks) {
            indeg.put(t.getTaskId(), 0);
        }
        for (TaskInput t : tasks) {
            List<Long> deps = Optional.ofNullable(t.getDependentTaskIds()).orElse(List.of());
            for (Long d : deps) {
                indeg.put(t.getTaskId(), indeg.getOrDefault(t.getTaskId(), 0) + 1);
                graph.computeIfAbsent(d, k -> new ArrayList<>()).add(t.getTaskId());
            }
        }
        Deque<Long> q = new ArrayDeque<>();
        for (TaskInput t : tasks) {
            if (indeg.getOrDefault(t.getTaskId(), 0) == 0)
                q.add(t.getTaskId());
        }
        List<TaskInput> ordered = new ArrayList<>();
        while (!q.isEmpty()) {
            Long u = q.removeFirst();
            TaskInput tu = idToTask.get(u);
            if (tu != null)
                ordered.add(tu);
            for (Long v : graph.getOrDefault(u, List.of())) {
                indeg.put(v, indeg.getOrDefault(v, 0) + (-1));
                if (indeg.get(v) == 0)
                    q.addLast(v);
            }
        }
        boolean acyclic = (ordered.size() == tasks.size());
        if (!acyclic) {
            return Pair.of(tasks, false);
        }
        ordered.sort((a, b) -> {
            long da = a.getDeadlineMs() == null ? Long.MAX_VALUE : a.getDeadlineMs();
            long db = b.getDeadlineMs() == null ? Long.MAX_VALUE : b.getDeadlineMs();
            if (da == db) {
                double pa = a.getPriorityScore() == null ? 0.0 : a.getPriorityScore();
                double pb = b.getPriorityScore() == null ? 0.0 : b.getPriorityScore();
                return Double.compare(pb, pa);
            }
            return Long.compare(da, db);
        });
        return Pair.of(ordered, true);
    }

    @Data
    @AllArgsConstructor
    private static class WindowState {
        Window win;
        int cursor;
    }

    @Data
    private static class BestCandidate {
        int index;
        int start;
        int end;
        double util;
        UtilityBreakdown breakdown;
        Integer cat;
        int cont;
        boolean ok;

        BestCandidate() {
            this.util = -1e18;
            this.ok = false;
        }
    }

    @Data
    private static class GreedyState {
        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> unscheduled = new ArrayList<>();
    }

    private GreedyState greedySchedule(List<TaskInput> ordered, List<Window> wins, Weights w) {
        GreedyState state = new GreedyState();
        List<WindowState> windowStates = new ArrayList<>();
        for (Window win : wins) {
            int start = Optional.ofNullable(win.getStartMin()).orElse(0);
            windowStates.add(new WindowState(win, start));
        }
        Map<Long, Integer> prevCatByDate = new HashMap<>();
        Map<Long, Assignment> placedByTask = new HashMap<>();
        Set<Long> unsSet = new HashSet<>();

        for (TaskInput t : ordered) {
            boolean depFailed = Optional.ofNullable(t.getDependentTaskIds()).orElse(List.of()).stream()
                    .anyMatch(unsSet::contains);
            if (depFailed) {
                state.unscheduled
                        .add(UnScheduleReason.builder().taskId(t.getTaskId()).reason("dependency unscheduled").build());
                unsSet.add(t.getTaskId());
                continue;
            }

            BestCandidate best = new BestCandidate();
            for (int i = 0; i < windowStates.size(); i++) {
                WindowState ws = windowStates.get(i);
                int need = Optional.ofNullable(t.getDurationMin()).orElse(0);
                Pair<Integer, Boolean> depEarliest = earliestStartForWindow(t, ws.win, placedByTask, unsSet);
                if (!depEarliest.getSecond())
                    continue;
                int start = Math.max(ws.cursor, depEarliest.getFirst());
                int winEnd = Optional.ofNullable(ws.win.getEndMin()).orElse(Integer.MAX_VALUE);
                if (start + need > winEnd)
                    continue;

                int cat = utilityModel.categoryKey(t);
                Integer prev = prevCatByDate.get(ws.win.getDateMs());
                int cont = 0;
                if (prev != null && prev == cat) {
                    int winStart = Optional.ofNullable(ws.win.getStartMin()).orElse(0);
                    cont = start - winStart;
                }
                Pair<Double, UtilityBreakdown> scored = utilityModel.scorePlacement(t, ws.win.getDateMs(), start,
                        start + need, prev, w, cont);
                double u = scored.getFirst();
                if (!best.ok || u > best.util) {
                    best.index = i;
                    best.start = start;
                    best.end = start + need;
                    best.util = u;
                    best.breakdown = scored.getSecond();
                    best.cat = cat;
                    best.cont = cont;
                    best.ok = true;
                }
            }
            if (!best.ok) {
                state.unscheduled.add(UnScheduleReason.builder().taskId(t.getTaskId())
                        .reason("no feasible window after dependency constraints").build());
                unsSet.add(t.getTaskId());
                continue;
            }
            WindowState chosen = windowStates.get(best.index);
            chosen.cursor = best.end;
            prevCatByDate.put(chosen.win.getDateMs(), best.cat);
            Assignment a = Assignment.builder()
                    .taskId(t.getTaskId())
                    .dateMs(chosen.win.getDateMs())
                    .startMin(best.start)
                    .endMin(best.end)
                    .utility(best.util)
                    .rationale(best.breakdown)
                    .build();
            state.assignments.add(a);
            placedByTask.put(t.getTaskId(), a);
        }
        return state;
    }

    private Pair<Integer, Boolean> earliestStartForWindow(TaskInput t, Window win, Map<Long, Assignment> placed,
            Set<Long> uns) {
        List<Long> deps = Optional.ofNullable(t.getDependentTaskIds()).orElse(List.of());
        for (Long d : deps) {
            if (uns.contains(d))
                return Pair.of(0, false);
        }
        int maxEnd = Optional.ofNullable(win.getStartMin()).orElse(0);
        for (Long d : deps) {
            Assignment a = placed.get(d);
            if (a == null) {
                // dep not placed yet -> disallow to be strict
                return Pair.of(0, false);
            }
            if (a.getDateMs() > win.getDateMs())
                return Pair.of(0, false);
            if (Objects.equals(a.getDateMs(), win.getDateMs())) {
                if (a.getEndMin() != null && a.getEndMin() > maxEnd)
                    maxEnd = a.getEndMin();
            }
        }
        int winStart = Optional.ofNullable(win.getStartMin()).orElse(0);
        if (maxEnd < winStart)
            maxEnd = winStart;
        return Pair.of(maxEnd, true);
    }

    // ------------ Local Search ------------

    private boolean overlaps(Assignment a, Assignment b) {
        if (!Objects.equals(a.getDateMs(), b.getDateMs()))
            return false;
        int aS = Optional.ofNullable(a.getStartMin()).orElse(0);
        int aE = Optional.ofNullable(a.getEndMin()).orElse(0);
        int bS = Optional.ofNullable(b.getStartMin()).orElse(0);
        int bE = Optional.ofNullable(b.getEndMin()).orElse(0);
        return !(aE <= bS || bE <= aS);
    }

    private boolean anyOverlap(List<Assignment> as) {
        for (int i = 0; i < as.size(); i++) {
            for (int j = i + 1; j < as.size(); j++) {
                if (overlaps(as.get(i), as.get(j)))
                    return true;
            }
        }
        return false;
    }

    private double totalUtility(List<Assignment> as) {
        return as.stream().map(a -> Optional.ofNullable(a.getUtility()).orElse(0.0)).reduce(0.0, Double::sum);
    }

    private boolean violatesDepsAfterSwap(Assignment a, Assignment b, Map<Long, List<Long>> deps) {
        // if a must precede b (a in deps[b]) then a.end <= b.start
        List<Long> bdeps = deps.getOrDefault(b.getTaskId(), List.of());
        if (bdeps.contains(a.getTaskId())) {
            return !(Optional.ofNullable(a.getEndMin()).orElse(0) <= Optional.ofNullable(b.getStartMin()).orElse(0));
        }
        List<Long> adeps = deps.getOrDefault(a.getTaskId(), List.of());
        if (adeps.contains(b.getTaskId())) {
            return !(Optional.ofNullable(b.getEndMin()).orElse(0) <= Optional.ofNullable(a.getStartMin()).orElse(0));
        }
        return false;
    }

    private List<Assignment> localSearchImprove(
            List<Assignment> assign,
            List<Window> wins,
            Weights w,
            Map<Long, TaskInput> tasks,
            Map<Long, List<Long>> deps,
            Duration budget,
            int step) {
        if (assign.isEmpty() || budget.isZero() || budget.isNegative())
            return assign;
        List<Assignment> best = new ArrayList<>(assign);
        double bestScore = totalUtility(best);
        Instant deadline = Instant.now().plus(budget);

        Map<Long, Window> winByDate = wins.stream().collect(Collectors.toMap(Window::getDateMs, x -> x, (a, b) -> a));
        Map<Long, Integer> idxByTask = new HashMap<>();
        for (int i = 0; i < best.size(); i++)
            idxByTask.put(best.get(i).getTaskId(), i);
        Map<Long, List<Long>> children = new HashMap<>();
        for (Map.Entry<Long, List<Long>> e : deps.entrySet()) {
            Long child = e.getKey();
            for (Long d : e.getValue()) {
                children.computeIfAbsent(d, k -> new ArrayList<>()).add(child);
            }
        }

        boolean improved = true;
        while (improved && Instant.now().isBefore(deadline)) {
            improved = false;
            // Shift moves
            for (int i = 0; i < best.size() && Instant.now().isBefore(deadline); i++) {
                Assignment a = best.get(i);
                Window win = winByDate.get(a.getDateMs());
                if (win == null)
                    continue;
                int dur = Optional.ofNullable(a.getEndMin()).orElse(0) - Optional.ofNullable(a.getStartMin()).orElse(0);

                // Try shift left then right
                int[] dirs = new int[] { -1, 1 };
                for (int dIdx = 0; dIdx < dirs.length && Instant.now().isBefore(deadline); dIdx++) {
                    int dir = dirs[dIdx];
                    int ns = Optional.ofNullable(a.getStartMin()).orElse(0) + dir * step;
                    while (Instant.now().isBefore(deadline)) {
                        int ne = ns + dur;
                        int wS = Optional.ofNullable(win.getStartMin()).orElse(0);
                        int wE = Optional.ofNullable(win.getEndMin()).orElse(Integer.MAX_VALUE);
                        if (ns < wS || ne > wE)
                            break;
                        boolean violate = false;
                        for (Long d : deps.getOrDefault(a.getTaskId(), List.of())) {
                            Integer j = idxByTask.get(d);
                            if (j != null) {
                                Assignment dep = best.get(j);
                                if (dep.getDateMs() > a.getDateMs() || (Objects.equals(dep.getDateMs(), a.getDateMs())
                                        && Optional.ofNullable(dep.getEndMin()).orElse(0) > ns)) {
                                    violate = true;
                                    break;
                                }
                            }
                        }
                        if (!violate) {
                            for (Long ch : children.getOrDefault(a.getTaskId(), List.of())) {
                                Integer j = idxByTask.get(ch);
                                if (j != null) {
                                    Assignment depd = best.get(j);
                                    if (depd.getDateMs() < a.getDateMs()
                                            || (Objects.equals(depd.getDateMs(), a.getDateMs())
                                                    && ne > Optional.ofNullable(depd.getStartMin()).orElse(0))) {
                                        violate = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (violate)
                            break;

                        List<Assignment> cand = new ArrayList<>(best);
                        Assignment a2 = Assignment.builder()
                                .taskId(a.getTaskId())
                                .dateMs(a.getDateMs())
                                .startMin(ns)
                                .endMin(ne)
                                .build();
                        TaskInput ti = tasks.get(a2.getTaskId());
                        Pair<Double, UtilityBreakdown> sc = utilityModel.scorePlacement(ti, a2.getDateMs(),
                                a2.getStartMin(), a2.getEndMin(), null, w, a2.getEndMin() - a2.getStartMin());
                        a2.setUtility(sc.getFirst());
                        a2.setRationale(sc.getSecond());
                        cand.set(i, a2);
                        if (anyOverlap(cand))
                            break;
                        double s = totalUtility(cand);
                        if (s > bestScore) {
                            best = cand;
                            bestScore = s;
                            a = a2;
                            ns = Optional.ofNullable(a.getStartMin()).orElse(0) + dir * step;
                            improved = true;
                        } else {
                            break;
                        }
                    }
                    // If improved, re-fetch references for next iteration
                    if (improved) {
                        win = winByDate.get(a.getDateMs());
                        dur = Optional.ofNullable(a.getEndMin()).orElse(0)
                                - Optional.ofNullable(a.getStartMin()).orElse(0);
                    }
                }
            }

            // Swap moves
            for (int i = 0; i < best.size() && Instant.now().isBefore(deadline); i++) {
                for (int j = i + 1; j < best.size() && Instant.now().isBefore(deadline); j++) {
                    Assignment a = best.get(i), b = best.get(j);
                    if (!Objects.equals(a.getDateMs(), b.getDateMs()))
                        continue;
                    if (violatesDepsAfterSwap(a, b, deps))
                        continue;

                    Assignment a2 = Assignment.builder()
                            .taskId(a.getTaskId()).dateMs(a.getDateMs()).startMin(b.getStartMin()).endMin(b.getEndMin())
                            .build();
                    Assignment b2 = Assignment.builder()
                            .taskId(b.getTaskId()).dateMs(b.getDateMs()).startMin(a.getStartMin()).endMin(a.getEndMin())
                            .build();
                    Window win = winByDate.get(a2.getDateMs());
                    int wS = Optional.ofNullable(win.getStartMin()).orElse(0);
                    int wE = Optional.ofNullable(win.getEndMin()).orElse(Integer.MAX_VALUE);
                    if (a2.getStartMin() < wS || a2.getEndMin() > wE)
                        continue;
                    if (b2.getStartMin() < wS || b2.getEndMin() > wE)
                        continue;

                    TaskInput ta = tasks.get(a2.getTaskId());
                    Pair<Double, UtilityBreakdown> scA = utilityModel.scorePlacement(ta, a2.getDateMs(),
                            a2.getStartMin(), a2.getEndMin(), null, w, a2.getEndMin() - a2.getStartMin());
                    a2.setUtility(scA.getFirst());
                    a2.setRationale(scA.getSecond());
                    TaskInput tb = tasks.get(b2.getTaskId());
                    Pair<Double, UtilityBreakdown> scB = utilityModel.scorePlacement(tb, b2.getDateMs(),
                            b2.getStartMin(), b2.getEndMin(), null, w, b2.getEndMin() - b2.getStartMin());
                    b2.setUtility(scB.getFirst());
                    b2.setRationale(scB.getSecond());

                    List<Assignment> cand = new ArrayList<>(best);
                    cand.set(i, a2);
                    cand.set(j, b2);
                    if (anyOverlap(cand))
                        continue;
                    double s = totalUtility(cand);
                    if (s > bestScore) {
                        best = cand;
                        bestScore = s;
                        improved = true;
                    }
                }
            }
        }
        return best;
    }
}
