/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.kernel.algorithm.milp;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.*;

import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.TaskInput;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Weights;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.PlanResult;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.UnScheduleReason;

/**
 * MILP daily scheduler on discretized time (slotMin). Uses start variables per
 * (task, slot)
 * with capacity and precedence constraints. Objective maximizes linear utility
 * composed of
 * priority and deadline lateness (in hours). Context switch and fatigue are
 * omitted to keep linearity.
 */
public class MilpScheduler {

    /**
     * Check if OR-Tools native libraries and a MIP solver are available.
     */
    public static boolean isAvailable() {
        try {
            Loader.loadNativeLibraries();
        } catch (Throwable t) {
            return false;
        }
        String[] candidates = new String[] { "SCIP", "CBC_MIXED_INTEGER_PROGRAMMING",
                "SCIP_MIXED_INTEGER_PROGRAMMING" };
        for (String name : candidates) {
            try {
                MPSolver s = MPSolver.createSolver(name);
                if (s != null)
                    return true;
            } catch (Throwable ignore) {
                // try next
            }
        }
        return false;
    }

    private static class SlotIndex {
        long dateMs;
        int minute;
    }

    private static List<SlotIndex> buildSlots(List<Window> wins, int slotMin) {
        List<SlotIndex> slots = new ArrayList<>();
        // preserve input windows order; ensure deterministic
        for (Window w : wins) {
            int start = w.getStartMin() == null ? 0 : w.getStartMin();
            int end = w.getEndMin() == null ? 0 : w.getEndMin();
            for (int m = start; m + slotMin <= end; m += slotMin) {
                SlotIndex si = new SlotIndex();
                si.dateMs = w.getDateMs();
                si.minute = m;
                slots.add(si);
            }
        }
        return slots;
    }

    private static boolean[] computeAllowedStarts(TaskInput task, List<Window> wins, List<SlotIndex> slots,
            int slotMin) {
        boolean[] allow = new boolean[slots.size()];
        int durSlots = (int) Math.ceil((task.getDurationMin() == null ? 0 : task.getDurationMin()) / (double) slotMin);
        for (int t = 0; t < slots.size(); t++) {
            SlotIndex s = slots.get(t);
            int startMin = s.minute;
            int endMin = startMin + (task.getDurationMin() == null ? 0 : task.getDurationMin());
            boolean fits = false;
            for (Window w : wins) {
                if (!Objects.equals(w.getDateMs(), s.dateMs))
                    continue;
                int wS = w.getStartMin() == null ? 0 : w.getStartMin();
                int wE = w.getEndMin() == null ? 0 : w.getEndMin();
                if (startMin >= wS && endMin <= wE) {
                    fits = true;
                    break;
                }
            }
            if (fits && t + durSlots <= slots.size()) {
                boolean contiguous = true;
                for (int k = 1; k < durSlots; k++) {
                    SlotIndex nxt = slots.get(t + k);
                    if (nxt.dateMs != s.dateMs || nxt.minute != s.minute + k * slotMin) {
                        contiguous = false;
                        break;
                    }
                }
                allow[t] = contiguous;
            } else {
                allow[t] = false;
            }
        }
        return allow;
    }

    private static double latenessHours(Long deadlineMs, long dateMs, int endMin) {
        if (deadlineMs == null)
            return 0.0;
        long endAbs = dateMs + endMin * 60_000L;
        if (endAbs <= deadlineMs)
            return 0.0;
        double minutes = (endAbs - deadlineMs) / 60_000.0;
        return minutes / 60.0; // normalize to hours
    }

    /**
     * Solve a daily schedule for the given windows using MILP. Returns PlanResult
     * with assignments
     * for scheduled tasks and unscheduled reasons for the rest. slotMin controls
     * discretization.
     */
    public PlanResult schedule(List<TaskInput> tasks, List<Window> wins, Weights weights, int slotMin) {
        Loader.loadNativeLibraries();
        List<SlotIndex> slots = buildSlots(wins, slotMin);
        int T = slots.size();
        int N = tasks.size();

        boolean[][] allow = new boolean[N][T];
        int[] durSlots = new int[N];
        double[][] score = new double[N][T];
        for (int i = 0; i < N; i++) {
            TaskInput task = tasks.get(i);
            int durMin = task.getDurationMin() == null ? 0 : task.getDurationMin();
            durSlots[i] = (int) Math.ceil(durMin / (double) slotMin);
            allow[i] = computeAllowedStarts(task, wins, slots, slotMin);
            for (int t = 0; t < T; t++) {
                if (!allow[i][t]) {
                    score[i][t] = -1e6;
                    continue;
                }
                SlotIndex s = slots.get(t);
                int endMin = s.minute + durMin;
                double pri = (weights.getWPriority() == null ? 0.0 : weights.getWPriority())
                        * (task.getPriorityScore() == null ? 0.0 : task.getPriorityScore());
                double dln = (weights.getWDeadline() == null ? 0.0 : weights.getWDeadline())
                        * latenessHours(task.getDeadlineMs(), s.dateMs, endMin);
                score[i][t] = pri - dln;
            }
        }

        // Try a few solver backends for portability
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null)
            solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null)
            solver = MPSolver.createSolver("SCIP_MIXED_INTEGER_PROGRAMMING");
        if (solver == null)
            throw new RuntimeException("No suitable OR-Tools MIP solver available (SCIP/CBC)");

        MPVariable[][] sVar = new MPVariable[N][T];
        for (int i = 0; i < N; i++) {
            for (int t = 0; t < T; t++) {
                sVar[i][t] = solver.makeIntVar(0, 1, "s_" + i + "_" + t);
            }
        }

        // One start per task
        for (int i = 0; i < N; i++) {
            MPConstraint c = solver.makeConstraint(0, 1, "oneStart_" + i);
            for (int t = 0; t < T; t++)
                c.setCoefficient(sVar[i][t], 1);
        }
        // Forbid disallowed starts
        for (int i = 0; i < N; i++) {
            for (int t = 0; t < T; t++) {
                if (!allow[i][t]) {
                    MPConstraint c = solver.makeConstraint(0, 0, "forbid_" + i + "_" + t);
                    c.setCoefficient(sVar[i][t], 1);
                }
            }
        }
        // Capacity per slot u
        for (int u = 0; u < T; u++) {
            MPConstraint cap = solver.makeConstraint(0, 1, "cap_" + u);
            for (int i = 0; i < N; i++) {
                for (int t = 0; t < T; t++) {
                    if (!allow[i][t])
                        continue;
                    if (t <= u && u < t + durSlots[i])
                        cap.setCoefficient(sVar[i][t], 1);
                }
            }
        }
        // Precedence constraints:
        // 1) gating: sum_t s_jt <= sum_t s_it (can't schedule child if parent not
        // scheduled)
        // 2) ordering: start(j) - start(i) >= durSlots[i]
        Map<Long, Integer> idxById = new HashMap<>();
        for (int i = 0; i < N; i++)
            idxById.put(tasks.get(i).getTaskId(), i);
        int bigM = T + Arrays.stream(durSlots).max().orElse(0) + 1;
        for (int j = 0; j < N; j++) {
            List<Long> deps = tasks.get(j).getDependentTaskIds();
            if (deps == null)
                continue;
            for (Long dep : deps) {
                Integer i = idxById.get(dep);
                if (i == null)
                    continue; // dep outside scope
                // gating: sum_t s_jt - sum_t s_it <= 0  (child can be 1 only if parent is 1)
                MPConstraint gate = solver.makeConstraint(-MPSolver.infinity(), 0, "gate_" + i + "_to_" + j);
                for (int t = 0; t < T; t++) {
                    gate.setCoefficient(sVar[j][t], 1);
                    gate.setCoefficient(sVar[i][t], -1);
                }
                // ordering with big-M relaxation when either task not scheduled:
                // sum_j (t - M) s_jt + sum_i (-t - M) s_it >= dur_i - 2M
                MPConstraint c = solver.makeConstraint(durSlots[i] - 2.0 * bigM, MPSolver.infinity(),
                        "prec_" + i + "_to_" + j);
                for (int t = 0; t < T; t++) {
                    c.setCoefficient(sVar[j][t], t - bigM);
                    c.setCoefficient(sVar[i][t], -t - bigM);
                }
            }
        }

        MPObjective obj = solver.objective();
        for (int i = 0; i < N; i++)
            for (int t = 0; t < T; t++)
                obj.setCoefficient(sVar[i][t], score[i][t]);
        obj.setMaximization();

        MPSolver.ResultStatus status = solver.solve();

        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> uns = new ArrayList<>();
        if (status != MPSolver.ResultStatus.OPTIMAL && status != MPSolver.ResultStatus.FEASIBLE) {
            for (TaskInput ti : tasks)
                uns.add(UnScheduleReason.builder().taskId(ti.getTaskId()).reason("solver infeasible").build());
            return PlanResult.builder().assignments(assignments).unScheduled(uns).build();
        }

        for (int i = 0; i < N; i++) {
            int chosenT = -1;
            double bestVal = -1;
            for (int t = 0; t < T; t++) {
                if (sVar[i][t].solutionValue() > 0.5) {
                    chosenT = t;
                    bestVal = score[i][t];
                    break;
                }
            }
            if (chosenT < 0) {
                uns.add(UnScheduleReason.builder().taskId(tasks.get(i).getTaskId()).reason("no feasible start")
                        .build());
                continue;
            }
            SlotIndex start = slots.get(chosenT);
            int durMin = tasks.get(i).getDurationMin() == null ? 0 : tasks.get(i).getDurationMin();
            int endMin = start.minute + durMin;
            assignments.add(Assignment.builder().taskId(tasks.get(i).getTaskId()).dateMs(start.dateMs)
                    .startMin(start.minute).endMin(endMin).utility(bestVal).build());
        }
        return PlanResult.builder().assignments(assignments).unScheduled(uns).build();
    }
}
