/*
Author: QuanTuanHuy
Description: Part of Serp Project - Abstract Optimal Scheduler (Partial Template Pattern)
*/

package serp.project.ptm_optimization.infrastructure.algorithm.base;

import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.utils.SchedulingUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for optimal schedulers (CP-SAT, MILP).
 * 
 * Uses Partial Template Method Pattern:
 * - Defines common workflow: validate → build model → solve → extract
 * - Concrete classes implement model-specific logic
 * - Provides utility methods for common operations
 * 
 * This pattern is suitable because CP-SAT and MILP share:
 * - Same high-level workflow (build model → solve → extract solution)
 * - Similar input validation requirements
 * - Common utility calculations (lateness, priority scoring)
 * - Same output format (PlanResult)
 * 
 * They differ in:
 * - Model representation (CpModel vs MPSolver)
 * - Variable types (IntervalVar vs MPVariable)
 * - Constraint formulation
 * - Solver configuration
 */
@Slf4j
public abstract class AbstractOptimalScheduler<TModel, TSolver, TStatus> {

    protected final SchedulingUtils schedulingUtils;
    protected Weights currentWeights;

    protected AbstractOptimalScheduler(SchedulingUtils schedulingUtils) {
        this.schedulingUtils = schedulingUtils;
    }

    // ==========================
    // TEMPLATE METHOD
    // ==========================

    /**
     * Main scheduling method using template pattern.
     * Defines the skeleton algorithm, delegates specifics to subclasses.
     */
    public final PlanResult schedule(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params
    ) {
        // 1. Pre-validation
        ValidationResult validation = validateInput(tasks, windows, params);
        if (!validation.isValid()) {
            return createFailureResult(tasks, validation.getReason());
        }

        // Store weights for utility calculation
        this.currentWeights = weights;

        // 2. Check solver availability
        if (!isSolverAvailable()) {
            return createFailureResult(tasks, getSolverName() + " solver not available");
        }

        // 3. Build model (abstract)
        long buildStart = System.currentTimeMillis();
        TModel model = buildModel(tasks, windows, weights, params);
        long buildTime = System.currentTimeMillis() - buildStart;
        log.debug("{}: Model built in {}ms", getSolverName(), buildTime);

        // 4. Configure and create solver (abstract)
        TSolver solver = createSolver(params);
        configureSolver(solver, params);

        // 5. Solve (abstract)
        log.info("{}: Starting solve with {} tasks, {} windows",
                getSolverName(), tasks.size(), windows.size());
        long solveStart = System.currentTimeMillis();
        TStatus status = solve(solver, model);
        long solveTime = System.currentTimeMillis() - solveStart;
        log.info("{}: Solve completed in {}ms with status: {}",
                getSolverName(), solveTime, status);

        // 6. Extract solution (abstract)
        return extractSolution(solver, status, model, tasks, weights);
    }

    // ==========================
    // ABSTRACT METHODS (to be implemented by subclasses)
    // ==========================

    /**
     * Get the solver name for logging.
     */
    protected abstract String getSolverName();

    /**
     * Check if the solver is available.
     */
    protected abstract boolean isSolverAvailable();

    /**
     * Build the optimization model.
     */
    protected abstract TModel buildModel(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params
    );

    /**
     * Create a new solver instance.
     */
    protected abstract TSolver createSolver(Params params);

    /**
     * Configure solver parameters.
     */
    protected abstract void configureSolver(TSolver solver, Params params);

    /**
     * Execute the solving process.
     */
    protected abstract TStatus solve(TSolver solver, TModel model);

    /**
     * Extract solution from solver after solving.
     */
    protected abstract PlanResult extractSolution(
            TSolver solver,
            TStatus status,
            TModel model,
            List<TaskInput> tasks,
            Weights weights
    );

    /**
     * Check if solver status indicates success.
     */
    protected abstract boolean isSuccessStatus(TStatus status);

    /**
     * Get failure reason from solver status.
     */
    protected abstract String getStatusReason(TStatus status);

    // ==========================
    // HOOK METHODS (can be overridden)
    // ==========================

    /**
     * Validate input before building model.
     * Can be overridden for solver-specific validation.
     */
    protected ValidationResult validateInput(
            List<TaskInput> tasks,
            List<Window> windows,
            Params params
    ) {
        if (tasks == null || tasks.isEmpty()) {
            return ValidationResult.invalid("No tasks provided");
        }

        if (windows == null || windows.isEmpty()) {
            return ValidationResult.invalid("No windows provided");
        }

        // Check for size limits
        int maxTasks = getMaxTasks();
        int maxSlots = getMaxSlots();

        if (tasks.size() > maxTasks) {
            return ValidationResult.invalid(String.format(
                    "%s: Too many tasks (%d > %d limit)",
                    getSolverName(), tasks.size(), maxTasks
            ));
        }

        // Estimate slots if needed
        int estimatedSlots = estimateSlotCount(windows, params);
        if (estimatedSlots > maxSlots) {
            return ValidationResult.invalid(String.format(
                    "%s: Too many slots (%d > %d limit)",
                    getSolverName(), estimatedSlots, maxSlots
            ));
        }

        return ValidationResult.valid();
    }

    /**
     * Get maximum recommended tasks for this solver.
     */
    protected abstract int getMaxTasks();

    /**
     * Get maximum recommended slots for this solver.
     */
    protected abstract int getMaxSlots();

    /**
     * Estimate number of time slots for validation.
     */
    protected int estimateSlotCount(List<Window> windows, Params params) {
        int slotMin = (params != null && params.getSlotMin() != null) ? params.getSlotMin() : 15;
        int totalMinutes = windows.stream()
                .mapToInt(w -> {
                    int start = w.getStartMin() != null ? w.getStartMin() : 0;
                    int end = w.getEndMin() != null ? w.getEndMin() : 1440;
                    return end - start;
                })
                .sum();
        return totalMinutes / slotMin;
    }

    // ==========================
    // UTILITY METHODS (shared by subclasses)
    // ==========================

    /**
     * Calculate utility score using shared SchedulingUtils.
     */
    protected double calculateUtility(
            TaskInput task,
            long dateMs,
            int startMin,
            int endMin,
            Weights weights
    ) {
        return schedulingUtils.calculateUtility(task, dateMs, startMin, endMin, weights);
    }

    /**
     * Calculate deadline score.
     */
    protected double calculateDeadlineScore(
            Long deadlineMs,
            long dateMs,
            int endMin,
            Weights weights
    ) {
        return schedulingUtils.calculateDeadlineScore(deadlineMs, dateMs, endMin, weights);
    }

    /**
     * Calculate lateness in hours.
     */
    protected double calculateLatenessHours(Long deadlineMs, long dateMs, int endMin) {
        return schedulingUtils.calculateLatenessHours(deadlineMs, dateMs, endMin);
    }

    /**
     * Create a failure result with all tasks unscheduled.
     */
    protected PlanResult createFailureResult(List<TaskInput> tasks, String reason) {
        List<UnScheduleReason> unscheduled = new ArrayList<>();
        for (TaskInput task : tasks) {
            unscheduled.add(UnScheduleReason.builder()
                    .taskId(task.getTaskId())
                    .reason(reason)
                    .build());
        }
        return PlanResult.builder()
                .assignments(new ArrayList<>())
                .unScheduled(unscheduled)
                .build();
    }

    /**
     * Create assignments list from scheduled task info.
     */
    protected List<Assignment> createAssignments(
            List<ScheduledTask> scheduledTasks,
            Weights weights
    ) {
        List<Assignment> assignments = new ArrayList<>();
        for (ScheduledTask st : scheduledTasks) {
            double utility = calculateUtility(st.task, st.dateMs, st.startMin, st.endMin, weights);
            assignments.add(Assignment.builder()
                    .taskId(st.task.getTaskId())
                    .dateMs(st.dateMs)
                    .startMin(st.startMin)
                    .endMin(st.endMin)
                    .utility(utility)
                    .build());
        }
        return assignments;
    }

    /**
     * Helper class to hold scheduled task info.
     */
    protected static class ScheduledTask {
        public final TaskInput task;
        public final long dateMs;
        public final int startMin;
        public final int endMin;

        public ScheduledTask(TaskInput task, long dateMs, int startMin, int endMin) {
            this.task = task;
            this.dateMs = dateMs;
            this.startMin = startMin;
            this.endMin = endMin;
        }
    }

    /**
     * Validation result holder.
     */
    protected static class ValidationResult {
        private final boolean valid;
        private final String reason;

        private ValidationResult(boolean valid, String reason) {
            this.valid = valid;
            this.reason = reason;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String reason) {
            return new ValidationResult(false, reason);
        }

        public boolean isValid() {
            return valid;
        }

        public String getReason() {
            return reason;
        }
    }
}
