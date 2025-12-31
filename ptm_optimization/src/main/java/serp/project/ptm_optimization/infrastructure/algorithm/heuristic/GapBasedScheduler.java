/*
Author: QuanTuanHuy
Description: Part of Serp Project - Gap-based Heuristic Scheduler using GapManager
*/

package serp.project.ptm_optimization.infrastructure.algorithm.heuristic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.kernel.utils.GapManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gap-based scheduling algorithm inspired by ptm_schedule's hybrid_scheduler.go.
 * Uses GapManager to calculate available time gaps and intelligently place tasks.
 * 
 * Advantages over cursor-based approach:
 * - Better gap utilization (fewer fragmented gaps)
 * - Global view of schedule quality
 * - Conflict detection for ripple effects
 * - Supports multiple tasks per window
 */
@Component
@Slf4j
public class GapBasedScheduler {
    
    private final GapManager gapManager;
    
    public GapBasedScheduler(GapManager gapManager) {
        this.gapManager = gapManager;
    }
    
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        // 1. Topological sort
        List<TaskInput> ordered = topologicalSort(tasks);
        if (ordered.isEmpty()) {
            return PlanResult.builder()
                .assignments(new ArrayList<>())
                .unScheduled(tasks.stream()
                    .map(t -> UnScheduleReason.builder()
                        .taskId(t.getTaskId())
                        .reason("dependency cycle detected")
                        .build())
                    .toList())
                .build();
        }
        
        // 2. Gap-based greedy scheduling
        List<Assignment> assignments = new ArrayList<>();
        List<UnScheduleReason> unscheduled = new ArrayList<>();
        Set<Long> unscheduledIds = new HashSet<>();
        Map<Long, Assignment> taskToAssignment = new HashMap<>();
        
        for (TaskInput task : ordered) {
            // Check dependency failures
            boolean depFailed = Optional.ofNullable(task.getDependentTaskIds())
                .orElse(List.of())
                .stream()
                .anyMatch(unscheduledIds::contains);
                
            if (depFailed) {
                unscheduled.add(UnScheduleReason.builder()
                    .taskId(task.getTaskId())
                    .reason("dependency unscheduled")
                    .build());
                unscheduledIds.add(task.getTaskId());
                continue;
            }
            
            // Find best gap for this task
            BestGapCandidate best = findBestGap(task, windows, assignments, taskToAssignment, weights);
            
            if (!best.isValid()) {
                unscheduled.add(UnScheduleReason.builder()
                    .taskId(task.getTaskId())
                    .reason("no suitable gap found")
                    .build());
                unscheduledIds.add(task.getTaskId());
                continue;
            }
            
            // Create assignment
            Assignment assignment = Assignment.builder()
                .taskId(task.getTaskId())
                .dateMs(best.getWindow().getDateMs())
                .startMin(best.getStartMin())
                .endMin(best.getEndMin())
                .utility(best.getUtility())
                .build();
                
            assignments.add(assignment);
            taskToAssignment.put(task.getTaskId(), assignment);
            
            log.debug("Scheduled task {} in gap [{}-{}] with utility {}", 
                task.getTaskId(), best.getStartMin(), best.getEndMin(), best.getUtility());
        }
        
        // 3. Calculate final fragmentation score
        double fragmentation = gapManager.calculateFragmentation(windows, assignments, 15);
        log.info("Schedule complete: {} tasks placed, {} unscheduled, fragmentation: {:.2f}%", 
            assignments.size(), unscheduled.size(), fragmentation * 100);
        
        return PlanResult.builder()
            .assignments(assignments)
            .unScheduled(unscheduled)
            .build();
    }
    
    /**
     * Find the best gap to place a task.
     * Considers: gap size, utility score, fragmentation impact.
     */
    private BestGapCandidate findBestGap(
        TaskInput task,
        List<Window> windows,
        List<Assignment> currentAssignments,
        Map<Long, Assignment> taskToAssignment,
        Weights weights
    ) {
        BestGapCandidate best = new BestGapCandidate();
        int taskDuration = Optional.ofNullable(task.getDurationMin()).orElse(0);
        
        Map<Long, List<Window>> windowsByDate = windows.stream()
            .collect(Collectors.groupingBy(Window::getDateMs));
        
        for (var entry : windowsByDate.entrySet()) {
            Long dateMs = entry.getKey();
            List<Window> dateWindows = entry.getValue();
            
            // Check dependency constraints for this date
            if (!canScheduleOnDate(task, dateMs, taskToAssignment)) {
                continue;
            }
            
            // Get assignments on this date
            List<Assignment> dateAssignments = currentAssignments.stream()
                .filter(a -> Objects.equals(a.getDateMs(), dateMs))
                .toList();
            
            // Calculate gaps for each window on this date
            for (Window window : dateWindows) {
                List<Window> gaps = gapManager.calculateGaps(List.of(window), dateAssignments);
                
                // Try to fit task in each gap
                for (Window gap : gaps) {
                    int gapDuration = gap.getEndMin() - gap.getStartMin();
                    if (gapDuration < taskDuration) {
                        continue; // Gap too small
                    }
                    
                    // Try different positions within gap (start, middle, end)
                    List<Integer> positions = calculatePositions(gap, taskDuration);
                    
                    for (int startMin : positions) {
                        int endMin = startMin + taskDuration;
                        
                        // Calculate utility for this placement
                        double utility = calculateUtility(task, window, dateMs, startMin, endMin, weights);
                        
                        // Penalize fragmentation
                        double fragmentationPenalty = calculateFragmentationPenalty(gap, startMin, endMin);
                        double adjustedUtility = utility - fragmentationPenalty;
                        
                        if (adjustedUtility > best.getUtility()) {
                            best.setWindow(window);
                            best.setStartMin(startMin);
                            best.setEndMin(endMin);
                            best.setUtility(adjustedUtility);
                            best.setValid(true);
                        }
                    }
                }
            }
        }
        
        return best;
    }
    
    /**
     * Check if task can be scheduled on given date (dependency constraints).
     */
    private boolean canScheduleOnDate(TaskInput task, Long dateMs, Map<Long, Assignment> taskToAssignment) {
        List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(List.of());
        
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
     * Calculate candidate positions within a gap (start, middle, end).
     */
    private List<Integer> calculatePositions(Window gap, int taskDuration) {
        List<Integer> positions = new ArrayList<>();
        
        int gapDuration = gap.getEndMin() - gap.getStartMin();
        
        // Start of gap
        positions.add(gap.getStartMin());
        
        // Middle of gap (if space allows)
        if (gapDuration >= taskDuration + 30) { // At least 30 min buffer
            int middle = gap.getStartMin() + (gapDuration - taskDuration) / 2;
            positions.add(middle);
        }
        
        // End of gap
        int end = gap.getEndMin() - taskDuration;
        if (end > gap.getStartMin()) {
            positions.add(end);
        }
        
        return positions;
    }
    
    /**
     * Calculate utility for placing task at specific time.
     */
    private double calculateUtility(
        TaskInput task,
        Window window,
        Long dateMs,
        int startMin,
        int endMin,
        Weights weights
    ) {
        double utility = 0.0;
        
        // Priority score
        Double priority = task.getPriorityScore();
        if (priority != null) {
            utility += priority * Optional.ofNullable(weights).map(Weights::getWPriority).orElse(1.0);
        }
        
        // Deadline bonus/penalty
        if (task.getDeadlineMs() != null) {
            long endAbsMs = dateMs + endMin * 60_000L;
            if (endAbsMs > task.getDeadlineMs()) {
                // Late - penalize
                long lateMs = endAbsMs - task.getDeadlineMs();
                double lateHours = lateMs / (60.0 * 60.0 * 1000.0);
                utility -= lateHours * 10.0 * Optional.ofNullable(weights).map(Weights::getWDeadline).orElse(1.0);
            } else {
                // Early - small bonus
                utility += 5.0;
            }
        }
        
        // Deep work bonus
        if (window.getIsDeepWork() != null && window.getIsDeepWork()) {
            Double effort = task.getEffort();
            if (effort != null && effort > 0.7) {
                utility += 20.0; // Bonus for high-effort tasks in deep work windows
            }
        }
        
        return utility;
    }
    
    /**
     * Calculate fragmentation penalty for placing task at position.
     * Penalize placements that create many small gaps.
     */
    private double calculateFragmentationPenalty(Window gap, int startMin, int endMin) {
        int gapStart = gap.getStartMin();
        int gapEnd = gap.getEndMin();
        
        // Calculate leftover gaps
        int leftGap = startMin - gapStart;
        int rightGap = gapEnd - endMin;
        
        double penalty = 0.0;
        
        // Penalize small gaps (< 15 min are unusable)
        if (leftGap > 0 && leftGap < 15) {
            penalty += 5.0;
        }
        if (rightGap > 0 && rightGap < 15) {
            penalty += 5.0;
        }
        
        // Prefer positions that minimize total fragmentation
        int totalFragmentation = Math.min(leftGap, 15) + Math.min(rightGap, 15);
        penalty += totalFragmentation * 0.1;
        
        return penalty;
    }
    
    /**
     * Topological sort with deadline/priority ordering.
     */
    private List<TaskInput> topologicalSort(List<TaskInput> tasks) {
        Map<Long, TaskInput> taskMap = tasks.stream()
            .collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
        
        Map<Long, Integer> inDegree = new HashMap<>();
        Map<Long, List<Long>> graph = new HashMap<>();
        
        // Initialize
        for (TaskInput task : tasks) {
            inDegree.put(task.getTaskId(), 0);
        }
        
        // Build graph
        for (TaskInput task : tasks) {
            List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(List.of());
            for (Long depId : deps) {
                inDegree.put(task.getTaskId(), inDegree.get(task.getTaskId()) + 1);
                graph.computeIfAbsent(depId, k -> new ArrayList<>()).add(task.getTaskId());
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
            
            for (Long childId : graph.getOrDefault(taskId, List.of())) {
                inDegree.put(childId, inDegree.get(childId) - 1);
                if (inDegree.get(childId) == 0) {
                    queue.add(childId);
                }
            }
        }
        
        // Check for cycles
        if (sorted.size() != tasks.size()) {
            log.warn("Dependency cycle detected: {} tasks, {} sorted", tasks.size(), sorted.size());
            return new ArrayList<>();
        }
        
        // Sort by deadline then priority
        sorted.sort((a, b) -> {
            long deadlineA = Optional.ofNullable(a.getDeadlineMs()).orElse(Long.MAX_VALUE);
            long deadlineB = Optional.ofNullable(b.getDeadlineMs()).orElse(Long.MAX_VALUE);
            if (deadlineA != deadlineB) {
                return Long.compare(deadlineA, deadlineB);
            }
            double priorityA = Optional.ofNullable(a.getPriorityScore()).orElse(0.0);
            double priorityB = Optional.ofNullable(b.getPriorityScore()).orElse(0.0);
            return Double.compare(priorityB, priorityA);
        });
        
        return sorted;
    }
    
    @Data
    @AllArgsConstructor
    private static class BestGapCandidate {
        private Window window;
        private int startMin;
        private int endMin;
        private double utility;
        private boolean valid;
        
        BestGapCandidate() {
            this.utility = Double.NEGATIVE_INFINITY;
            this.valid = false;
        }
    }
}
