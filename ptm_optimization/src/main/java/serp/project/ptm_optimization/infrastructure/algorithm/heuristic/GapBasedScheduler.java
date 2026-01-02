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
import serp.project.ptm_optimization.kernel.utils.SchedulingUtils;

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
    private final SchedulingUtils schedulingUtils;
    
    public GapBasedScheduler(GapManager gapManager, SchedulingUtils schedulingUtils) {
        this.gapManager = gapManager;
        this.schedulingUtils = schedulingUtils;
    }
    
    public PlanResult schedule(List<TaskInput> tasks, List<Window> windows, Weights weights, Params params) {
        // 1. Topological sort using shared utility
        List<TaskInput> ordered = schedulingUtils.topologicalSort(tasks);
        if (ordered.isEmpty() && !tasks.isEmpty()) {
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
            // Check dependency failures using shared utility
            boolean depFailed = schedulingUtils.hasUnscheduledDependency(task, unscheduledIds);
                
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
            
            // Check dependency constraints for this date using shared utility
            if (!schedulingUtils.canScheduleOnDate(task, dateMs, taskToAssignment)) {
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
                        
                        // Calculate utility using shared utility
                        double utility = schedulingUtils.calculateUtility(task, dateMs, startMin, endMin, weights, window);
                        
                        // Penalize fragmentation using shared utility
                        double fragmentationPenalty = schedulingUtils.calculateFragmentationPenalty(
                            gap.getStartMin(), gap.getEndMin(), startMin, endMin);
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
