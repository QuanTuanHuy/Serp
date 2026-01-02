/*
Author: QuanTuanHuy
Description: Part of Serp Project - MILP Scheduler using Partial Template Pattern
*/

package serp.project.ptm_optimization.infrastructure.algorithm.milp;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.infrastructure.algorithm.base.AbstractOptimalScheduler;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.utils.SchedulingUtils;

import java.util.*;

/**
 * MILP daily scheduler on discretized time (slotMin).
 * Extends AbstractOptimalScheduler to leverage shared utilities and template pattern.
 * 
 * Uses start variables per (task, slot) with capacity and precedence constraints.
 * Objective maximizes linear utility composed of priority and deadline lateness.
 */
@Component
@Slf4j
public class MilpSchedulerV2 extends AbstractOptimalScheduler<MilpSchedulerV2.MilpModel, MPSolver, MPSolver.ResultStatus> {

    private static final int MAX_TASKS_FOR_MILP = 30;
    private static final int MAX_SLOTS_FOR_MILP = 500;
    private static final int MAX_VARIABLES_FOR_MILP = 15000;

    public MilpSchedulerV2(SchedulingUtils schedulingUtils) {
        super(schedulingUtils);
    }

    /**
     * Check if OR-Tools native libraries and a MIP solver are available.
     */
    public static boolean isAvailable() {
        try {
            Loader.loadNativeLibraries();
        } catch (Throwable t) {
            return false;
        }
        String[] candidates = new String[]{"SCIP", "CBC_MIXED_INTEGER_PROGRAMMING", "SCIP_MIXED_INTEGER_PROGRAMMING"};
        for (String name : candidates) {
            try {
                MPSolver s = MPSolver.createSolver(name);
                if (s != null) return true;
            } catch (Throwable ignore) {
            }
        }
        return false;
    }

    // ==========================
    // TEMPLATE METHOD IMPLEMENTATIONS
    // ==========================

    @Override
    protected String getSolverName() {
        return "MILP";
    }

    @Override
    protected boolean isSolverAvailable() {
        return isAvailable();
    }

    @Override
    protected int getMaxTasks() {
        return MAX_TASKS_FOR_MILP;
    }

    @Override
    protected int getMaxSlots() {
        return MAX_SLOTS_FOR_MILP;
    }

    @Override
    protected ValidationResult validateInput(List<TaskInput> tasks, List<Window> windows, Params params) {
        // Call parent validation first
        ValidationResult parentResult = super.validateInput(tasks, windows, params);
        if (!parentResult.isValid()) {
            return parentResult;
        }

        // Additional MILP-specific validation
        int slotMin = (params != null && params.getSlotMin() != null) ? params.getSlotMin() : 15;
        int totalSlots = estimateSlotCount(windows, params);
        long totalVariables = (long) tasks.size() * totalSlots;

        if (totalVariables > MAX_VARIABLES_FOR_MILP) {
            return ValidationResult.invalid(String.format(
                    "MILP: Too many variables (%d tasks × %d slots = %d > %d limit)",
                    tasks.size(), totalSlots, totalVariables, MAX_VARIABLES_FOR_MILP
            ));
        }

        return ValidationResult.valid();
    }

    @Override
    protected MilpModel buildModel(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params
    ) {
        int slotMin = (params != null && params.getSlotMin() != null) ? params.getSlotMin() : 15;
        return new MilpModel(tasks, windows, weights, slotMin, schedulingUtils);
    }

    @Override
    protected MPSolver createSolver(Params params) {
        // Try different solver backends
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) solver = MPSolver.createSolver("CBC_MIXED_INTEGER_PROGRAMMING");
        if (solver == null) solver = MPSolver.createSolver("SCIP_MIXED_INTEGER_PROGRAMMING");
        
        if (solver == null) {
            throw new RuntimeException("No suitable OR-Tools MIP solver available (SCIP/CBC)");
        }
        
        return solver;
    }

    @Override
    protected void configureSolver(MPSolver solver, Params params) {
        // MILP solver configuration can be done here if needed
        log.debug("Configured MILP solver");
    }

    @Override
    protected MPSolver.ResultStatus solve(MPSolver solver, MilpModel model) {
        // Build solver model
        model.buildSolverModel(solver);
        
        log.info("MILP: Starting solve with {} tasks, {} slots, {} variables",
                model.tasks.size(), model.slots.size(), model.tasks.size() * model.slots.size());
        
        return solver.solve();
    }

    @Override
    protected boolean isSuccessStatus(MPSolver.ResultStatus status) {
        return status == MPSolver.ResultStatus.OPTIMAL || status == MPSolver.ResultStatus.FEASIBLE;
    }

    @Override
    protected String getStatusReason(MPSolver.ResultStatus status) {
        return "Solver status: " + status;
    }

    @Override
    protected PlanResult extractSolution(
            MPSolver solver,
            MPSolver.ResultStatus status,
            MilpModel model,
            List<TaskInput> tasks,
            Weights weights
    ) {
        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> unscheduled = new ArrayList<>();

        if (!isSuccessStatus(status)) {
            for (TaskInput ti : tasks) {
                unscheduled.add(UnScheduleReason.builder()
                        .taskId(ti.getTaskId())
                        .reason("solver infeasible")
                        .build());
            }
            return PlanResult.builder()
                    .assignments(assignments)
                    .unScheduled(unscheduled)
                    .build();
        }

        // Extract solution from variables
        for (int i = 0; i < model.tasks.size(); i++) {
            TaskInput task = model.tasks.get(i);
            int chosenSlot = -1;
            double bestScore = -1;

            for (int t = 0; t < model.slots.size(); t++) {
                if (model.sVar[i][t].solutionValue() > 0.5) {
                    chosenSlot = t;
                    bestScore = model.score[i][t];
                    break;
                }
            }

            if (chosenSlot < 0) {
                unscheduled.add(UnScheduleReason.builder()
                        .taskId(task.getTaskId())
                        .reason("no feasible start")
                        .build());
                continue;
            }

            SlotIndex slot = model.slots.get(chosenSlot);
            int durMin = task.getDurationMin() != null ? task.getDurationMin() : 0;
            int endMin = slot.minute + durMin;

            // Use shared utility calculation
            double utility = calculateUtility(task, slot.dateMs, slot.minute, endMin, weights);

            assignments.add(Assignment.builder()
                    .taskId(task.getTaskId())
                    .dateMs(slot.dateMs)
                    .startMin(slot.minute)
                    .endMin(endMin)
                    .utility(utility)
                    .build());
        }

        return PlanResult.builder()
                .assignments(assignments)
                .unScheduled(unscheduled)
                .build();
    }

    // ==========================
    // INNER CLASSES
    // ==========================

    /**
     * Represents a time slot for MILP discretization.
     */
    static class SlotIndex {
        long dateMs;
        int minute;
    }

    /**
     * Encapsulates MILP model construction logic.
     */
    protected static class MilpModel {
        final List<TaskInput> tasks;
        final List<Window> windows;
        final Weights weights;
        final int slotMin;
        final SchedulingUtils schedulingUtils;

        // Computed data
        List<SlotIndex> slots;
        boolean[][] allow;
        int[] durSlots;
        double[][] score;
        MPVariable[][] sVar;

        MilpModel(List<TaskInput> tasks, List<Window> windows, Weights weights, 
                  int slotMin, SchedulingUtils schedulingUtils) {
            this.tasks = tasks;
            this.windows = windows;
            this.weights = weights;
            this.slotMin = slotMin;
            this.schedulingUtils = schedulingUtils;

            buildSlots();
            computeAllowedStarts();
            computeScores();
        }

        private void buildSlots() {
            slots = new ArrayList<>();
            for (Window w : windows) {
                int start = w.getStartMin() != null ? w.getStartMin() : 0;
                int end = w.getEndMin() != null ? w.getEndMin() : 1440;
                for (int m = start; m + slotMin <= end; m += slotMin) {
                    SlotIndex si = new SlotIndex();
                    si.dateMs = w.getDateMs();
                    si.minute = m;
                    slots.add(si);
                }
            }
        }

        private void computeAllowedStarts() {
            int N = tasks.size();
            int T = slots.size();
            allow = new boolean[N][T];
            durSlots = new int[N];

            for (int i = 0; i < N; i++) {
                TaskInput task = tasks.get(i);
                int durMin = task.getDurationMin() != null ? task.getDurationMin() : 0;
                durSlots[i] = (int) Math.ceil(durMin / (double) slotMin);

                for (int t = 0; t < T; t++) {
                    SlotIndex s = slots.get(t);
                    int startMin = s.minute;
                    int endMin = startMin + durMin;

                    // Check if task fits within any window
                    boolean fits = false;
                    for (Window w : windows) {
                        if (!Objects.equals(w.getDateMs(), s.dateMs)) continue;
                        int wS = w.getStartMin() != null ? w.getStartMin() : 0;
                        int wE = w.getEndMin() != null ? w.getEndMin() : 1440;
                        if (startMin >= wS && endMin <= wE) {
                            fits = true;
                            break;
                        }
                    }

                    // Check contiguous slots
                    if (fits && t + durSlots[i] <= T) {
                        boolean contiguous = true;
                        for (int k = 1; k < durSlots[i]; k++) {
                            SlotIndex nxt = slots.get(t + k);
                            if (nxt.dateMs != s.dateMs || nxt.minute != s.minute + k * slotMin) {
                                contiguous = false;
                                break;
                            }
                        }
                        allow[i][t] = contiguous;
                    } else {
                        allow[i][t] = false;
                    }
                }
            }
        }

        private void computeScores() {
            int N = tasks.size();
            int T = slots.size();
            score = new double[N][T];

            double wPriority = weights != null && weights.getWPriority() != null ? weights.getWPriority() : 1.0;
            double wDeadline = weights != null && weights.getWDeadline() != null ? weights.getWDeadline() : 1.0;

            for (int i = 0; i < N; i++) {
                TaskInput task = tasks.get(i);
                int durMin = task.getDurationMin() != null ? task.getDurationMin() : 0;

                for (int t = 0; t < T; t++) {
                    if (!allow[i][t]) {
                        score[i][t] = -1e6;
                        continue;
                    }

                    SlotIndex s = slots.get(t);
                    int endMin = s.minute + durMin;

                    // Use shared utility for lateness calculation
                    double pri = wPriority * (task.getPriorityScore() != null ? task.getPriorityScore() : 0.0);
                    double lateness = schedulingUtils.calculateLatenessHours(task.getDeadlineMs(), s.dateMs, endMin);
                    double dln = wDeadline * lateness;

                    score[i][t] = pri - dln;
                }
            }
        }

        void buildSolverModel(MPSolver solver) {
            int N = tasks.size();
            int T = slots.size();

            // Create variables
            sVar = new MPVariable[N][T];
            for (int i = 0; i < N; i++) {
                for (int t = 0; t < T; t++) {
                    sVar[i][t] = solver.makeIntVar(0, 1, "s_" + i + "_" + t);
                }
            }

            // Constraint: One start per task (at most)
            for (int i = 0; i < N; i++) {
                MPConstraint c = solver.makeConstraint(0, 1, "oneStart_" + i);
                for (int t = 0; t < T; t++) {
                    c.setCoefficient(sVar[i][t], 1);
                }
            }

            // Constraint: Forbid disallowed starts
            for (int i = 0; i < N; i++) {
                for (int t = 0; t < T; t++) {
                    if (!allow[i][t]) {
                        MPConstraint c = solver.makeConstraint(0, 0, "forbid_" + i + "_" + t);
                        c.setCoefficient(sVar[i][t], 1);
                    }
                }
            }

            // Constraint: Capacity per slot (no overlaps)
            for (int u = 0; u < T; u++) {
                MPConstraint cap = solver.makeConstraint(0, 1, "cap_" + u);
                for (int i = 0; i < N; i++) {
                    for (int t = 0; t < T; t++) {
                        if (!allow[i][t]) continue;
                        if (t <= u && u < t + durSlots[i]) {
                            cap.setCoefficient(sVar[i][t], 1);
                        }
                    }
                }
            }

            // Precedence constraints
            addPrecedenceConstraints(solver, N, T);

            // Objective: maximize score
            MPObjective obj = solver.objective();
            for (int i = 0; i < N; i++) {
                for (int t = 0; t < T; t++) {
                    obj.setCoefficient(sVar[i][t], score[i][t]);
                }
            }
            obj.setMaximization();
        }

        private void addPrecedenceConstraints(MPSolver solver, int N, int T) {
            Map<Long, Integer> idxById = new HashMap<>();
            for (int i = 0; i < N; i++) {
                idxById.put(tasks.get(i).getTaskId(), i);
            }

            int bigM = T + Arrays.stream(durSlots).max().orElse(0) + 1;

            for (int j = 0; j < N; j++) {
                List<Long> deps = tasks.get(j).getDependentTaskIds();
                if (deps == null) continue;

                for (Long dep : deps) {
                    Integer i = idxById.get(dep);
                    if (i == null) continue;

                    // Gating constraint: child can only be scheduled if parent is scheduled
                    MPConstraint gate = solver.makeConstraint(-MPSolver.infinity(), 0, "gate_" + i + "_to_" + j);
                    for (int t = 0; t < T; t++) {
                        gate.setCoefficient(sVar[j][t], 1);
                        gate.setCoefficient(sVar[i][t], -1);
                    }

                    // Ordering constraint with big-M
                    MPConstraint c = solver.makeConstraint(durSlots[i] - 2.0 * bigM, MPSolver.infinity(),
                            "prec_" + i + "_to_" + j);
                    for (int t = 0; t < T; t++) {
                        c.setCoefficient(sVar[j][t], t - bigM);
                        c.setCoefficient(sVar[i][t], -t - bigM);
                    }
                }
            }
        }
    }
}
