/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - CP-SAT Scheduler using Partial Template Pattern
 */

package serp.project.ptm_optimization.infrastructure.algorithm.cpsat;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
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
import java.util.stream.Collectors;

/**
 * CP-SAT (Constraint Programming - SAT based) scheduler using Google OR-Tools.
 * Extends AbstractOptimalScheduler to leverage shared utilities and template pattern.
 * 
 * Superior to MILP for scheduling due to:
 * - Native interval variables
 * - Built-in no-overlap constraints (AddNoOverlap)
 * - Better scalability for disjunctive scheduling
 * - SAT-based search heuristics
 */
@Component
@Slf4j
public class CpSatSchedulerV2 extends AbstractOptimalScheduler<CpSatSchedulerV2.CpSatModel, CpSolver, CpSolverStatus> {

    private static final int DEFAULT_MAX_TIME_SECONDS = 30;
    private static final int DEFAULT_NUM_WORKERS = 4;
    private static final int MAX_TASKS = 100;
    private static final int MAX_SLOTS = 1000;

    public CpSatSchedulerV2(SchedulingUtils schedulingUtils) {
        super(schedulingUtils);
    }

    /**
     * Check if CP-SAT solver is available.
     */
    public static boolean isAvailable() {
        try {
            Loader.loadNativeLibraries();
            return true;
        } catch (Throwable t) {
            log.warn("CP-SAT not available: {}", t.getMessage());
            return false;
        }
    }

    // ==========================
    // TEMPLATE METHOD IMPLEMENTATIONS
    // ==========================

    @Override
    protected String getSolverName() {
        return "CP-SAT";
    }

    @Override
    protected boolean isSolverAvailable() {
        return isAvailable();
    }

    @Override
    protected int getMaxTasks() {
        return MAX_TASKS;
    }

    @Override
    protected int getMaxSlots() {
        return MAX_SLOTS;
    }

    @Override
    protected CpSatModel buildModel(
            List<TaskInput> tasks,
            List<Window> windows,
            Weights weights,
            Params params
    ) {
        CpModel model = new CpModel();
        CpSatModel cpSatModel = new CpSatModel(model, tasks, windows, weights, params);
        cpSatModel.build();
        return cpSatModel;
    }

    @Override
    protected CpSolver createSolver(Params params) {
        return new CpSolver();
    }

    @Override
    protected void configureSolver(CpSolver solver, Params params) {
        // Note: CpSolver configuration is done via SatParameters during solve
        // Logging configuration
        log.debug("Configured CP-SAT solver");
    }

    @Override
    protected CpSolverStatus solve(CpSolver solver, CpSatModel model) {
        // Configure solver parameters
        int maxTime = (model.params != null && model.params.getMaxTimeSec() != null)
                ? model.params.getMaxTimeSec()
                : DEFAULT_MAX_TIME_SECONDS;

        // Build parameters
        SatParameters.Builder solverParams = SatParameters.newBuilder();
        solverParams.setMaxTimeInSeconds(maxTime);
        solverParams.setNumSearchWorkers(DEFAULT_NUM_WORKERS);
        solverParams.setLogSearchProgress(true);
        solverParams.setLogToStdout(false);
        solverParams.setSearchBranching(SatParameters.SearchBranching.AUTOMATIC_SEARCH);
        solverParams.setCpModelPresolve(true);
        solverParams.setCpModelProbingLevel(2);

        log.info("CP-SAT: Starting solve with {} tasks, {} constraints, max_time={}s",
                model.tasks.size(), model.model.model().getConstraintsCount(), maxTime);

        return solver.solve(model.model);
    }

    @Override
    protected boolean isSuccessStatus(CpSolverStatus status) {
        return status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE;
    }

    @Override
    protected String getStatusReason(CpSolverStatus status) {
        return switch (status) {
            case INFEASIBLE -> "Problem is infeasible (no solution exists)";
            case MODEL_INVALID -> "Model is invalid (check constraints)";
            case UNKNOWN -> "Solver timeout or resource limit";
            default -> "Solver failed: " + status;
        };
    }

    @Override
    protected PlanResult extractSolution(
            CpSolver solver,
            CpSolverStatus status,
            CpSatModel model,
            List<TaskInput> tasks,
            Weights weights
    ) {
        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> unscheduled = new ArrayList<>();

        if (isSuccessStatus(status)) {
            log.info("CP-SAT: Solution found, objective={}, wall_time={}s",
                    solver.objectiveValue(), solver.wallTime());

            // Extract scheduled tasks
            for (TaskInput task : tasks) {
                IntVar startVar = model.getTaskStart(task.getTaskId());
                if (startVar == null) continue;

                long startMin = solver.value(startVar);
                long durationMin = task.getDurationMin() != null ? task.getDurationMin() : 0;
                long endMin = startMin + durationMin;

                Long dateMs = model.getTaskDateMs(task.getTaskId(), solver);
                if (dateMs == null) {
                    log.warn("Task {} scheduled but date unknown", task.getTaskId());
                    continue;
                }

                // Calculate utility using shared utility
                double utility = calculateUtility(task, dateMs, (int) startMin, (int) endMin, weights);

                assignments.add(Assignment.builder()
                        .taskId(task.getTaskId())
                        .dateMs(dateMs)
                        .startMin((int) startMin)
                        .endMin((int) endMin)
                        .utility(utility)
                        .build());

                log.debug("Task {} scheduled: date={}, start={}, end={}",
                        task.getTaskId(), dateMs, startMin, endMin);
            }

            // Find unscheduled tasks
            Set<Long> scheduledIds = assignments.stream()
                    .map(Assignment::getTaskId)
                    .collect(Collectors.toSet());

            for (TaskInput task : tasks) {
                if (!scheduledIds.contains(task.getTaskId())) {
                    unscheduled.add(UnScheduleReason.builder()
                            .taskId(task.getTaskId())
                            .reason("Not scheduled by CP-SAT solver")
                            .build());
                }
            }
        } else {
            // Solver failed
            String reason = getStatusReason(status);
            for (TaskInput task : tasks) {
                unscheduled.add(UnScheduleReason.builder()
                        .taskId(task.getTaskId())
                        .reason(reason)
                        .build());
            }
        }

        return PlanResult.builder()
                .assignments(assignments)
                .unScheduled(unscheduled)
                .build();
    }

    // ==========================
    // INNER MODEL BUILDER CLASS
    // ==========================

    /**
     * Encapsulates CP-SAT model construction logic.
     */
    protected static class CpSatModel {
        final CpModel model;
        final List<TaskInput> tasks;
        final List<Window> windows;
        final Weights weights;
        final Params params;

        // Variables
        private final Map<Long, IntervalVar> taskIntervals = new HashMap<>();
        private final Map<Long, IntVar> taskStarts = new HashMap<>();
        private final Map<Long, IntVar> taskEnds = new HashMap<>();
        private final Map<Long, Map<Long, Literal>> taskInWindow = new HashMap<>();

        CpSatModel(CpModel model, List<TaskInput> tasks, List<Window> windows,
                   Weights weights, Params params) {
            this.model = model;
            this.tasks = tasks;
            this.windows = windows;
            this.weights = weights;
            this.params = params;
        }

        void build() {
            createVariables();
            addNoOverlapConstraint();
            addPrecedenceConstraints();
            addWindowConstraints();
            defineObjective();
        }

        private void createVariables() {
            // Calculate global min/max from all windows
            long minStart = windows.stream()
                    .mapToLong(w -> w.getStartMin() != null ? w.getStartMin() : 0)
                    .min().orElse(0);
            long maxEnd = windows.stream()
                    .mapToLong(w -> w.getEndMin() != null ? w.getEndMin() : 1440)
                    .max().orElse(1440);

            for (TaskInput task : tasks) {
                int duration = task.getDurationMin() != null ? task.getDurationMin() : 0;

                // Create start variable
                IntVar start = model.newIntVar(minStart, maxEnd, "start_" + task.getTaskId());
                taskStarts.put(task.getTaskId(), start);

                // Create end variable
                IntVar end = model.newIntVar(minStart + duration, maxEnd, "end_" + task.getTaskId());
                taskEnds.put(task.getTaskId(), end);

                // Create interval variable
                IntervalVar interval = model.newIntervalVar(
                        start,
                        LinearExpr.constant(duration),
                        end,
                        "interval_" + task.getTaskId()
                );
                taskIntervals.put(task.getTaskId(), interval);
            }
        }

        private void addNoOverlapConstraint() {
            model.addNoOverlap(taskIntervals.values());
            log.debug("Added NoOverlap constraint for {} tasks", taskIntervals.size());
        }

        private void addPrecedenceConstraints() {
            int count = 0;
            for (TaskInput task : tasks) {
                if (task.getDependentTaskIds() == null) continue;

                for (Long depId : task.getDependentTaskIds()) {
                    IntVar depEnd = taskEnds.get(depId);
                    IntVar taskStart = taskStarts.get(task.getTaskId());

                    if (depEnd != null && taskStart != null) {
                        model.addLessOrEqual(depEnd, taskStart);
                        count++;
                    }
                }
            }
            log.debug("Added {} precedence constraints", count);
        }

        private void addWindowConstraints() {
            Map<Long, List<Window>> windowsByDate = windows.stream()
                    .collect(Collectors.groupingBy(Window::getDateMs));

            for (TaskInput task : tasks) {
                List<Literal> windowLiterals = new ArrayList<>();
                Map<Long, Literal> dateMap = new HashMap<>();

                for (var entry : windowsByDate.entrySet()) {
                    Long dateMs = entry.getKey();
                    List<Window> dateWindows = entry.getValue();

                    Literal onDate = model.newBoolVar("task_" + task.getTaskId() + "_on_date_" + dateMs);
                    windowLiterals.add(onDate);
                    dateMap.put(dateMs, onDate);

                    for (Window w : dateWindows) {
                        int wStart = w.getStartMin() != null ? w.getStartMin() : 0;
                        int wEnd = w.getEndMin() != null ? w.getEndMin() : 1440;

                        IntVar taskStart = taskStarts.get(task.getTaskId());
                        IntVar taskEnd = taskEnds.get(task.getTaskId());

                        model.addGreaterOrEqual(taskStart, wStart).onlyEnforceIf(onDate);
                        model.addLessOrEqual(taskEnd, wEnd).onlyEnforceIf(onDate);
                    }
                }

                taskInWindow.put(task.getTaskId(), dateMap);
                model.addBoolOr(windowLiterals);
            }
        }

        private void defineObjective() {
            LinearExprBuilder objective = LinearExpr.newBuilder();

            for (TaskInput task : tasks) {
                double priority = task.getPriorityScore() != null ? task.getPriorityScore() : 1.0;
                double weight = weights != null && weights.getPriorityWeight() != null 
                        ? weights.getPriorityWeight() : 1.0;

                IntVar start = taskStarts.get(task.getTaskId());
                objective.addTerm(start, (long) (priority * weight * 0.01));
            }

            model.maximize(objective);
        }

        IntVar getTaskStart(Long taskId) {
            return taskStarts.get(taskId);
        }

        Long getTaskDateMs(Long taskId, CpSolver solver) {
            Map<Long, Literal> dateMap = taskInWindow.get(taskId);
            if (dateMap == null) return null;

            for (var entry : dateMap.entrySet()) {
                if (solver.booleanValue(entry.getValue())) {
                    return entry.getKey();
                }
            }
            return null;
        }
    }
}
