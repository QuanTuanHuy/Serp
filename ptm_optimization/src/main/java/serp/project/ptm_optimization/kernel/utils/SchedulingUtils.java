/*
Author: QuanTuanHuy
Description: Part of Serp Project - Scheduling Utility Functions
*/

package serp.project.ptm_optimization.kernel.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Common utility functions for scheduling algorithms.
 * Extracted to reduce code duplication across Heuristic, LocalSearch, CP-SAT, and MILP schedulers.
 */
@Component
@Slf4j
public class SchedulingUtils {

    // ==========================
    // UTILITY SCORE CALCULATION
    // ==========================

    /**
     * Calculate utility score for placing a task at a specific time slot.
     * Higher score = better placement.
     *
     * @param task      The task to schedule
     * @param dateMs    The date (epoch ms)
     * @param startMin  Start time in minutes from midnight
     * @param endMin    End time in minutes from midnight
     * @param weights   Optimization weights
     * @param window    The window this task is placed in (optional, for deep work bonus)
     * @return Utility score
     */
    public double calculateUtility(
            TaskInput task,
            long dateMs,
            int startMin,
            int endMin,
            Weights weights,
            Window window
    ) {
        double utility = 0.0;

        // 1. Priority score contribution
        Double priority = task.getPriorityScore();
        if (priority != null) {
            double wPriority = getWeight(weights, Weights::getWPriority, Weights::getPriorityWeight, 1.0);
            utility += priority * wPriority;
        }

        // 2. Deadline bonus/penalty
        utility += calculateDeadlineScore(task.getDeadlineMs(), dateMs, endMin, weights);

        // 3. Deep work bonus (high-effort tasks in deep work windows)
        if (window != null && Boolean.TRUE.equals(window.getIsDeepWork())) {
            Double effort = task.getEffort();
            if (effort != null && effort > 0.7) {
                utility += 20.0; // Bonus for high-effort tasks in deep work windows
            }
        }

        return utility;
    }

    /**
     * Overloaded version without window (for simpler calculations).
     */
    public double calculateUtility(
            TaskInput task,
            long dateMs,
            int startMin,
            int endMin,
            Weights weights
    ) {
        return calculateUtility(task, dateMs, startMin, endMin, weights, null);
    }

    /**
     * Calculate deadline-related score component.
     * Returns bonus for early completion, penalty for late completion.
     *
     * @param deadlineMs Task deadline in epoch ms (nullable)
     * @param dateMs     Scheduled date in epoch ms
     * @param endMin     End time in minutes from midnight
     * @param weights    Optimization weights
     * @return Score adjustment (+bonus or -penalty)
     */
    public double calculateDeadlineScore(Long deadlineMs, long dateMs, int endMin, Weights weights) {
        if (deadlineMs == null) {
            return 0.0;
        }

        long endAbsMs = dateMs + (long) endMin * 60_000L;
        double wDeadline = getWeight(weights, Weights::getWDeadline, Weights::getDeadlineWeight, 1.0);

        if (endAbsMs > deadlineMs) {
            // Late - apply penalty
            double lateHours = (endAbsMs - deadlineMs) / (60.0 * 60.0 * 1000.0);
            return -lateHours * 10.0 * wDeadline;
        } else {
            // Early - small bonus
            return 5.0;
        }
    }

    /**
     * Calculate lateness in hours (for MILP scoring).
     *
     * @param deadlineMs Task deadline in epoch ms
     * @param dateMs     Scheduled date in epoch ms
     * @param endMin     End time in minutes from midnight
     * @return Lateness in hours (0 if on time or early)
     */
    public double calculateLatenessHours(Long deadlineMs, long dateMs, int endMin) {
        if (deadlineMs == null) {
            return 0.0;
        }

        long endAbsMs = dateMs + (long) endMin * 60_000L;
        if (endAbsMs <= deadlineMs) {
            return 0.0;
        }

        double lateMs = endAbsMs - deadlineMs;
        return lateMs / (60.0 * 60.0 * 1000.0); // Convert to hours
    }

    // ==========================
    // TOPOLOGICAL SORTING
    // ==========================

    /**
     * Perform topological sort on tasks based on dependencies.
     * Uses Kahn's algorithm with deadline/priority tie-breaking.
     *
     * @param tasks List of tasks with potential dependencies
     * @return Sorted list respecting dependencies, or empty list if cycle detected
     */
    public List<TaskInput> topologicalSort(List<TaskInput> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, TaskInput> taskMap = tasks.stream()
                .collect(Collectors.toMap(TaskInput::getTaskId, t -> t));

        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, List<Long>> graph = new HashMap<>();

        // Initialize in-degrees
        for (TaskInput task : tasks) {
            inDegree.put(task.getTaskId(), 0);
        }

        // Build dependency graph and count in-degrees
        for (TaskInput task : tasks) {
            List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(Collections.emptyList());
            for (Long depId : deps) {
                if (taskMap.containsKey(depId)) {
                    inDegree.merge(task.getTaskId(), 1, Integer::sum);
                    graph.computeIfAbsent(depId, k -> new ArrayList<>()).add(task.getTaskId());
                }
            }
        }

        // Kahn's algorithm
        Queue<Long> queue = new ArrayDeque<>();
        for (TaskInput task : tasks) {
            if (inDegree.get(task.getTaskId()) == 0) {
                queue.add(task.getTaskId());
            }
        }

        List<TaskInput> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            Long taskId = queue.poll();
            TaskInput task = taskMap.get(taskId);
            if (task != null) {
                sorted.add(task);
            }

            for (Long childId : graph.getOrDefault(taskId, Collections.emptyList())) {
                int newDegree = inDegree.get(childId) - 1;
                inDegree.put(childId, newDegree);
                if (newDegree == 0) {
                    queue.add(childId);
                }
            }
        }

        // Check for cycles
        if (sorted.size() != tasks.size()) {
            log.warn("Dependency cycle detected: {} tasks, {} sorted", tasks.size(), sorted.size());
            return Collections.emptyList();
        }

        // Secondary sort by deadline then priority
        sorted.sort((a, b) -> {
            long deadlineA = Optional.ofNullable(a.getDeadlineMs()).orElse(Long.MAX_VALUE);
            long deadlineB = Optional.ofNullable(b.getDeadlineMs()).orElse(Long.MAX_VALUE);
            if (deadlineA != deadlineB) {
                return Long.compare(deadlineA, deadlineB);
            }
            double priorityA = Optional.ofNullable(a.getPriorityScore()).orElse(0.0);
            double priorityB = Optional.ofNullable(b.getPriorityScore()).orElse(0.0);
            return Double.compare(priorityB, priorityA); // Higher priority first
        });

        return sorted;
    }

    // ==========================
    // DEPENDENCY VALIDATION
    // ==========================

    /**
     * Check if all dependencies of a task are satisfied for scheduling on a given date.
     *
     * @param task             The task to check
     * @param dateMs           Target date
     * @param taskToAssignment Map of already scheduled tasks
     * @return true if all dependencies are scheduled before or on the same date
     */
    public boolean canScheduleOnDate(
            TaskInput task,
            Long dateMs,
            Map<Long, Assignment> taskToAssignment
    ) {
        List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(Collections.emptyList());

        for (Long depId : deps) {
            Assignment depAssignment = taskToAssignment.get(depId);
            if (depAssignment == null) {
                return false; // Dependency not scheduled yet
            }
            if (depAssignment.getDateMs() > dateMs) {
                return false; // Dependency scheduled after this date
            }
        }

        return true;
    }

    /**
     * Check if dependencies are satisfied for a specific assignment.
     * Used for feasibility checking in local search.
     *
     * @param assignment       The assignment to validate
     * @param task             The task being assigned
     * @param taskToAssignment Map of all assignments
     * @return true if all dependencies finish before this assignment starts
     */
    public boolean dependenciesSatisfied(
            Assignment assignment,
            TaskInput task,
            Map<Long, Assignment> taskToAssignment
    ) {
        List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(Collections.emptyList());

        for (Long depId : deps) {
            Assignment depAssignment = taskToAssignment.get(depId);

            if (depAssignment == null) {
                return false; // Dependency not scheduled
            }

            // Dependency must finish before this task starts
            if (depAssignment.getDateMs() > assignment.getDateMs()) {
                return false;
            }

            if (depAssignment.getDateMs().equals(assignment.getDateMs()) &&
                    depAssignment.getEndMin() > assignment.getStartMin()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if any dependencies of a task failed to be scheduled.
     *
     * @param task            The task to check
     * @param unscheduledIds  Set of task IDs that failed scheduling
     * @return true if any dependency is in the unscheduled set
     */
    public boolean hasUnscheduledDependency(TaskInput task, Set<Long> unscheduledIds) {
        return Optional.ofNullable(task.getDependentTaskIds())
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(unscheduledIds::contains);
    }

    // ==========================
    // WINDOW VALIDATION
    // ==========================

    /**
     * Check if an assignment fits within any of the given windows.
     *
     * @param assignment The assignment to validate
     * @param windows    Available time windows
     * @return true if assignment is within at least one window
     */
    public boolean isWithinAnyWindow(Assignment assignment, List<Window> windows) {
        return windows.stream()
                .anyMatch(w ->
                        Objects.equals(w.getDateMs(), assignment.getDateMs()) &&
                                assignment.getStartMin() >= (w.getStartMin() != null ? w.getStartMin() : 0) &&
                                assignment.getEndMin() <= (w.getEndMin() != null ? w.getEndMin() : 1440)
                );
    }

    // ==========================
    // FRAGMENTATION ANALYSIS
    // ==========================

    /**
     * Calculate fragmentation penalty for placing a task in a gap.
     * Penalizes placements that create small unusable gaps.
     *
     * @param gapStartMin   Gap start time
     * @param gapEndMin     Gap end time
     * @param taskStartMin  Task start time within gap
     * @param taskEndMin    Task end time within gap
     * @param minUsableGap  Minimum gap size considered usable (default 15 min)
     * @return Penalty score (higher = worse)
     */
    public double calculateFragmentationPenalty(
            int gapStartMin,
            int gapEndMin,
            int taskStartMin,
            int taskEndMin,
            int minUsableGap
    ) {
        int leftGap = taskStartMin - gapStartMin;
        int rightGap = gapEndMin - taskEndMin;

        double penalty = 0.0;

        // Penalize small gaps (unusable)
        if (leftGap > 0 && leftGap < minUsableGap) {
            penalty += 5.0;
        }
        if (rightGap > 0 && rightGap < minUsableGap) {
            penalty += 5.0;
        }

        // Additional penalty for total fragmentation
        int totalFragmentation = Math.min(leftGap, minUsableGap) + Math.min(rightGap, minUsableGap);
        penalty += totalFragmentation * 0.1;

        return penalty;
    }

    /**
     * Overloaded version with default minUsableGap of 15 minutes.
     */
    public double calculateFragmentationPenalty(
            int gapStartMin,
            int gapEndMin,
            int taskStartMin,
            int taskEndMin
    ) {
        return calculateFragmentationPenalty(gapStartMin, gapEndMin, taskStartMin, taskEndMin, 15);
    }

    // ==========================
    // HELPER METHODS
    // ==========================

    /**
     * Safely get weight value, trying multiple getter methods.
     * Handles both getWPriority() and getPriorityWeight() style methods.
     */
    @FunctionalInterface
    public interface WeightGetter {
        Double get(Weights weights);
    }

    private double getWeight(Weights weights, WeightGetter getter1, WeightGetter getter2, double defaultValue) {
        if (weights == null) {
            return defaultValue;
        }

        try {
            Double value = getter1.get(weights);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }

        try {
            Double value = getter2.get(weights);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }

        return defaultValue;
    }

    /**
     * Build task ID to TaskInput map for quick lookups.
     */
    public Map<Long, TaskInput> buildTaskMap(List<TaskInput> tasks) {
        return tasks.stream()
                .collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
    }

    /**
     * Build task ID to Assignment map from assignments list.
     */
    public Map<Long, Assignment> buildAssignmentMap(List<Assignment> assignments) {
        return assignments.stream()
                .collect(Collectors.toMap(Assignment::getTaskId, a -> a));
    }

    /**
     * Group windows by date for efficient lookup.
     */
    public Map<Long, List<Window>> groupWindowsByDate(List<Window> windows) {
        return windows.stream()
                .collect(Collectors.groupingBy(Window::getDateMs));
    }

    /**
     * Group assignments by date for efficient lookup.
     */
    public Map<Long, List<Assignment>> groupAssignmentsByDate(List<Assignment> assignments) {
        return assignments.stream()
                .collect(Collectors.groupingBy(Assignment::getDateMs));
    }
}
