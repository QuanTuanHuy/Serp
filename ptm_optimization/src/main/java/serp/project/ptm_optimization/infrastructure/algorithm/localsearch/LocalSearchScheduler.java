/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local Search Scheduler (Simulated Annealing)
*/

package serp.project.ptm_optimization.infrastructure.algorithm.localsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Params;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.PlanResult;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.UnScheduleReason;
import serp.project.ptm_optimization.infrastructure.algorithm.heuristic.GapBasedScheduler;
import serp.project.ptm_optimization.kernel.utils.GapManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Local search scheduler using Simulated Annealing.
 * 
 * Algorithm:
 * 1. Start with GapBased solution (fast, good quality)
 * 2. Iteratively apply local moves (SWAP, SHIFT, REORDER)
 * 3. Accept worse solutions with probability exp(-delta/T) to escape local optima
 * 4. Gradually decrease temperature (cooling schedule)
 * 5. Return best solution found
 * 
 * Advantages:
 * - Refines GapBased solution (better quality)
 * - Fast local moves (~O(1) feasibility checks)
 * - Escapes local optima via annealing
 * - Scales to 1000+ tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LocalSearchScheduler {
    
    private final GapBasedScheduler gapBasedScheduler;
    private final GapManager gapManager;
    private final Random random = new Random();
    
    public PlanResult schedule(
        List<TaskInput> tasks,
        List<Window> windows,
        Weights weights,
        Params params
    ) {
        // 1. Get initial solution from GapBased
        PlanResult initial = gapBasedScheduler.schedule(tasks, windows, weights, params);
        
        if (initial.getAssignments().isEmpty()) {
            return initial; // No solution to improve
        }
        
        // 2. Simulated Annealing improvement
        return simulatedAnnealing(initial, tasks, windows, weights, params);
    }
    
    private PlanResult simulatedAnnealing(
        PlanResult initial,
        List<TaskInput> tasks,
        List<Window> windows,
        Weights weights,
        Params params
    ) {
        ScheduleState current = new ScheduleState(initial.getAssignments(), tasks);
        ScheduleState best = current.copy();
        
        // Annealing parameters
        double temperature = params.getInitialTemperature() != null 
            ? params.getInitialTemperature() : 1000.0;
        double coolingRate = params.getCoolingRate() != null 
            ? params.getCoolingRate() : 0.95;
        int maxIterations = params.getMaxIterations() != null 
            ? params.getMaxIterations() : 1000;
        
        double currentScore = objectiveFunction(current, tasks, windows, weights);
        double bestScore = currentScore;
        
        log.info("LocalSearch: initial score={:.2f}, temp={:.1f}, iterations={}", 
            currentScore, temperature, maxIterations);
        
        int improvements = 0;
        int accepted = 0;
        
        for (int iter = 0; iter < maxIterations; iter++) {
            // Generate neighbor
            Move move = selectRandomMove(current, tasks, windows);
            
            if (move == null) {
                continue; // No valid move found
            }
            
            // Apply move to create neighbor
            ScheduleState neighbor = current.copy();
            applyMove(neighbor, move, tasks);
            
            // Check feasibility
            if (!isFeasible(neighbor, tasks, windows)) {
                continue; // Skip infeasible neighbor
            }
            
            // Calculate delta
            double neighborScore = objectiveFunction(neighbor, tasks, windows, weights);
            double delta = neighborScore - currentScore;
            
            // Accept move?
            boolean accept = delta > 0 || random.nextDouble() < Math.exp(delta / temperature);
            
            if (accept) {
                current = neighbor;
                currentScore = neighborScore;
                accepted++;
                
                if (currentScore > bestScore) {
                    best = current.copy();
                    bestScore = currentScore;
                    improvements++;
                    
                    log.debug("Iteration {}: New best score={:.2f} (delta={:.2f})", 
                        iter, bestScore, delta);
                }
            }
            
            // Cool down
            temperature *= coolingRate;
            
            // Early termination if temperature too low
            if (temperature < 0.01) {
                log.debug("Temperature too low, terminating at iteration {}", iter);
                break;
            }
        }
        
        log.info("LocalSearch complete: best={:.2f}, initial={:.2f}, improvements={}, accepted={}", 
            bestScore, objectiveFunction(new ScheduleState(initial.getAssignments(), tasks), tasks, windows, weights),
            improvements, accepted);
        
        // Convert best state back to PlanResult
        return toPlanResult(best, tasks);
    }
    
    /**
     * Select a random move from the neighborhood.
     */
    private Move selectRandomMove(ScheduleState state, List<TaskInput> tasks, List<Window> windows) {
        if (state.getAssignments().size() < 2) {
            return null; // Need at least 2 assignments for moves
        }
        
        // Move distribution: 50% swap, 50% shift
        double r = random.nextDouble();
        
        if (r < 0.5) {
            return generateRandomSwap(state);
        } else {
            return generateRandomShift(state, windows);
        }
    }
    
    /**
     * Generate random SWAP move.
     */
    private Move generateRandomSwap(ScheduleState state) {
        List<Assignment> assignments = state.getAssignments();
        
        if (assignments.size() < 2) {
            return null;
        }
        
        int idx1 = random.nextInt(assignments.size());
        int idx2 = random.nextInt(assignments.size());
        
        while (idx1 == idx2) {
            idx2 = random.nextInt(assignments.size());
        }
        
        return Move.swap(
            assignments.get(idx1).getTaskId(),
            assignments.get(idx2).getTaskId()
        );
    }
    
    /**
     * Generate random SHIFT move.
     */
    private Move generateRandomShift(ScheduleState state, List<Window> windows) {
        List<Assignment> assignments = state.getAssignments();
        
        if (assignments.isEmpty() || windows.isEmpty()) {
            return null;
        }
        
        // Pick random assignment
        Assignment assignment = assignments.get(random.nextInt(assignments.size()));
        
        // Pick random window
        Window window = windows.get(random.nextInt(windows.size()));
        
        // Pick random start time within window
        int windowDuration = window.getEndMin() - window.getStartMin();
        int taskDuration = assignment.getEndMin() - assignment.getStartMin();
        
        if (windowDuration < taskDuration) {
            return null; // Window too small
        }
        
        int maxStart = window.getEndMin() - taskDuration;
        int targetStartMin = window.getStartMin() + random.nextInt(Math.max(1, maxStart - window.getStartMin() + 1));
        
        return Move.shift(assignment.getTaskId(), window.getDateMs(), targetStartMin);
    }
    
    /**
     * Apply move to schedule state.
     */
    private void applyMove(ScheduleState state, Move move, List<TaskInput> tasks) {
        switch (move.getType()) {
            case SWAP:
                state.swapAssignments(move.getTaskId1(), move.getTaskId2());
                break;
                
            case SHIFT:
                Assignment assignment = state.getAssignment(move.getTaskId());
                if (assignment != null) {
                    int duration = assignment.getEndMin() - assignment.getStartMin();
                    assignment.setDateMs(move.getTargetDateMs());
                    assignment.setStartMin(move.getTargetStartMin());
                    assignment.setEndMin(move.getTargetStartMin() + duration);
                    state.rebuild();
                }
                break;
                
            case REORDER:
                // Not implemented yet
                break;
        }
    }
    
    /**
     * Check if schedule state is feasible.
     */
    private boolean isFeasible(ScheduleState state, List<TaskInput> tasks, List<Window> windows) {
        Map<Long, TaskInput> taskMap = tasks.stream()
            .collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
        
        for (Assignment assignment : state.getAssignments()) {
            TaskInput task = taskMap.get(assignment.getTaskId());
            
            if (task == null) {
                return false;
            }
            
            // Check window constraints
            if (!isWithinAnyWindow(assignment, windows)) {
                return false;
            }
            
            // Check overlaps
            if (hasOverlaps(assignment, state)) {
                return false;
            }
            
            // Check dependencies
            if (!dependenciesSatisfied(assignment, task, state)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if assignment is within any window.
     */
    private boolean isWithinAnyWindow(Assignment assignment, List<Window> windows) {
        return windows.stream()
            .anyMatch(w -> 
                Objects.equals(w.getDateMs(), assignment.getDateMs()) &&
                assignment.getStartMin() >= w.getStartMin() &&
                assignment.getEndMin() <= w.getEndMin()
            );
    }
    
    /**
     * Check if assignment overlaps with any other assignment.
     */
    private boolean hasOverlaps(Assignment assignment, ScheduleState state) {
        return state.getAssignmentsOnDate(assignment.getDateMs()).stream()
            .filter(other -> !other.getTaskId().equals(assignment.getTaskId()))
            .anyMatch(other -> gapManager.overlaps(assignment, other));
    }
    
    /**
     * Check if dependencies are satisfied.
     */
    private boolean dependenciesSatisfied(Assignment assignment, TaskInput task, ScheduleState state) {
        List<Long> deps = Optional.ofNullable(task.getDependentTaskIds()).orElse(Collections.emptyList());
        
        for (Long depId : deps) {
            Assignment depAssignment = state.getAssignment(depId);
            
            if (depAssignment == null) {
                return false; // Dependency not scheduled
            }
            
            // Dependency must be before this task
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
     * Calculate objective function score.
     * Higher is better.
     */
    private double objectiveFunction(
        ScheduleState state,
        List<TaskInput> tasks,
        List<Window> windows,
        Weights weights
    ) {
        double score = 0.0;
        
        Map<Long, TaskInput> taskMap = tasks.stream()
            .collect(Collectors.toMap(TaskInput::getTaskId, t -> t));
        
        // 1. Priority score
        for (Assignment assignment : state.getAssignments()) {
            TaskInput task = taskMap.get(assignment.getTaskId());
            if (task != null && task.getPriorityScore() != null) {
                score += task.getPriorityScore() * weights.getWPriority();
            }
        }
        
        // 2. Deadline penalties
        for (Assignment assignment : state.getAssignments()) {
            TaskInput task = taskMap.get(assignment.getTaskId());
            if (task != null && task.getDeadlineMs() != null) {
                long endAbsMs = assignment.getDateMs() + assignment.getEndMin() * 60_000L;
                if (endAbsMs > task.getDeadlineMs()) {
                    long lateMs = endAbsMs - task.getDeadlineMs();
                    double lateHours = lateMs / (60.0 * 60.0 * 1000.0);
                    score -= lateHours * 10.0 * weights.getWDeadline();
                }
            }
        }
        
        // 3. Fragmentation penalty
        double fragmentation = gapManager.calculateFragmentation(windows, state.getAssignments(), 15);
        score -= fragmentation * 2.0; // Penalize fragmented schedules
        
        // 4. Number of scheduled tasks (maximize coverage)
        score += state.getAssignments().size() * 5.0;
        
        return score;
    }
    
    /**
     * Convert ScheduleState back to PlanResult.
     */
    private PlanResult toPlanResult(ScheduleState state, List<TaskInput> tasks) {
        List<UnScheduleReason> unscheduled = tasks.stream()
            .filter(task -> !state.isScheduled(task.getTaskId()))
            .map(task -> UnScheduleReason.builder()
                .taskId(task.getTaskId())
                .reason("not scheduled by local search")
                .build())
            .collect(Collectors.toList());
        
        return PlanResult.builder()
            .assignments(state.getAssignments())
            .unScheduled(unscheduled)
            .build();
    }
}
