/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local Search Schedule State
*/

package serp.project.ptm_optimization.infrastructure.algorithm.localsearch;

import lombok.Data;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wrapper around schedule state for local search operations.
 * Provides fast lookups and modification tracking.
 */
@Data
public class ScheduleState {
    
    private List<Assignment> assignments;
    private Map<Long, Assignment> taskToAssignment;
    private Map<Long, List<Assignment>> assignmentsByDate;
    private Set<Long> scheduledTaskIds;
    private Set<Long> unscheduledTaskIds;
    
    public ScheduleState(List<Assignment> assignments, List<TaskInput> allTasks) {
        this.assignments = new ArrayList<>(assignments);
        this.taskToAssignment = assignments.stream()
            .collect(Collectors.toMap(Assignment::getTaskId, a -> a));
        this.assignmentsByDate = assignments.stream()
            .collect(Collectors.groupingBy(Assignment::getDateMs));
        this.scheduledTaskIds = assignments.stream()
            .map(Assignment::getTaskId)
            .collect(Collectors.toSet());
        this.unscheduledTaskIds = allTasks.stream()
            .map(TaskInput::getTaskId)
            .filter(id -> !scheduledTaskIds.contains(id))
            .collect(Collectors.toSet());
    }
    
    /**
     * Deep copy constructor for creating neighbor states.
     */
    public ScheduleState copy() {
        ScheduleState copy = new ScheduleState();
        copy.assignments = new ArrayList<>();
        for (Assignment a : this.assignments) {
            copy.assignments.add(Assignment.builder()
                .taskId(a.getTaskId())
                .dateMs(a.getDateMs())
                .startMin(a.getStartMin())
                .endMin(a.getEndMin())
                .utility(a.getUtility())
                .build());
        }
        copy.rebuild();
        return copy;
    }
    
    private ScheduleState() {
        // Private constructor for copy()
    }
    
    /**
     * Rebuild lookup maps after modifications.
     */
    public void rebuild() {
        this.taskToAssignment = assignments.stream()
            .collect(Collectors.toMap(Assignment::getTaskId, a -> a));
        this.assignmentsByDate = assignments.stream()
            .collect(Collectors.groupingBy(Assignment::getDateMs));
        this.scheduledTaskIds = assignments.stream()
            .map(Assignment::getTaskId)
            .collect(Collectors.toSet());
    }
    
    /**
     * Get assignment for task.
     */
    public Assignment getAssignment(Long taskId) {
        return taskToAssignment.get(taskId);
    }
    
    /**
     * Get assignments on specific date.
     */
    public List<Assignment> getAssignmentsOnDate(Long dateMs) {
        return assignmentsByDate.getOrDefault(dateMs, Collections.emptyList());
    }
    
    /**
     * Check if task is scheduled.
     */
    public boolean isScheduled(Long taskId) {
        return scheduledTaskIds.contains(taskId);
    }
    
    /**
     * Update assignment.
     */
    public void updateAssignment(Assignment newAssignment) {
        // Remove old
        assignments.removeIf(a -> a.getTaskId().equals(newAssignment.getTaskId()));
        // Add new
        assignments.add(newAssignment);
        rebuild();
    }
    
    /**
     * Swap two assignments' time slots.
     */
    public void swapAssignments(Long taskId1, Long taskId2) {
        Assignment a1 = getAssignment(taskId1);
        Assignment a2 = getAssignment(taskId2);
        
        if (a1 == null || a2 == null) {
            return;
        }
        
        // Swap date and time
        Long tempDate = a1.getDateMs();
        Integer tempStart = a1.getStartMin();
        Integer tempEnd = a1.getEndMin();
        
        a1.setDateMs(a2.getDateMs());
        a1.setStartMin(a2.getStartMin());
        a1.setEndMin(a2.getEndMin());
        
        a2.setDateMs(tempDate);
        a2.setStartMin(tempStart);
        a2.setEndMin(tempEnd);
        
        rebuild();
    }
}
