/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.service.schedule.priority.OptimizationSchedulingPriorityStrategy;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Greedy implementation of the {@link OptimizationSchedulingPolicy}.
 *
 * This class uses a Greedy List Scheduling algorithm to assign start and end times
 * to work items. The scheduling process respects:
 * 1. Topological order / Task dependencies (tasks are scheduled only after all their predecessors are scheduled).
 * 2. Resource availability (tasks are scheduled during the assigned resource's available capacity slots).
 * 3. Priority ordering (ready tasks are prioritized using a configurable {@link OptimizationSchedulingPriorityStrategy}).
 */
@Service
public class GreedySchedulingPolicy implements OptimizationSchedulingPolicy {

    /**
     * Executes the greedy scheduling algorithm for the given project model.
     * It iteratively schedules ready work items to the earliest available capacity slots of their assignees.
     *
     * @param projectModel {@inheritDoc}
     * @param options      {@inheritDoc}
     * @param assignments  {@inheritDoc}
     * @param warnings     {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Map<Long, OptimizationScheduleSuggestion> generateSchedules(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            Map<Long, OptimizationAssignmentSuggestion> assignments,
            List<OptimizationConstraintViolation> warnings) {
        
        // Guard check: Skip scheduling if scheduling is not requested in the change scope
        if (!options.intent().changeScope().includesScheduling()) {
            return Map.of();
        }
        
        // Guard check: Stop if the dependency graph contains cycles to prevent infinite loops
        if (projectModel.dependencyGraph().hasCycles()) {
            return Map.of();
        }

        // Map work items by their ID for O(1) lookups during scheduling
        Map<Long, OptimizationWorkItem> itemById = projectModel.workItems().stream()
                .collect(Collectors.toMap(item -> item.workItem().getId(), item -> item));
        
        // Group capacity slots by assignee and sort them chronologically by slot start time
        Map<Long, List<ResourceCapacitySlot>> slotsByAssignee = projectModel.capacitySlots().stream()
                .collect(Collectors.groupingBy(ResourceCapacitySlot::assigneeId));
        slotsByAssignee.values().forEach(slots -> slots.sort(Comparator.comparing(ResourceCapacitySlot::slotStart)));

        // State tracking:
        // - Track already reserved time ranges per capacity slot to avoid double-booking assignees
        Map<SlotKey, List<TimeRange>> reservationsBySlot = new HashMap<>();
        // - Track the scheduled end times of tasks to determine the start times of their successors
        Map<Long, Long> scheduledEndByWorkItem = new HashMap<>();
        // - Stores the final schedule suggestion for each scheduled work item
        Map<Long, OptimizationScheduleSuggestion> schedules = new LinkedHashMap<>();
        // - The ready queue containing work items that have no unscheduled predecessors
        Queue<Long> ready = readyQueue(projectModel, itemById, options.schedulingPriorityStrategy());
        // - Keep count of remaining unscheduled predecessor tasks for each work item
        Map<Long, Integer> remainingPredecessors = remainingPredecessors(projectModel, itemById.keySet());

        // Process tasks from the queue one by one in priority order
        while (!ready.isEmpty()) {
            Long workItemId = ready.poll();
            OptimizationWorkItem item = itemById.get(workItemId);
            
            // Skip if the item doesn't exist or is already completed (no scheduling needed)
            if (item == null || item.done()) {
                markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
                continue;
            }
            
            // Resolve the resource assigned to this work item
            Long assigneeId = resolveScheduleAssignee(item, assignments);
            if (assigneeId == null) {
                // Generate a warning constraint violation if no assignee is found
                OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                        OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE,
                        workItemId,
                        "Schedule skipped because no assignee is available",
                        null
                );
                warnings.add(violation);
                // Estimate its end time assuming it starts at the earliest possible time but without resource capacity slots
                scheduledEndByWorkItem.put(workItemId, earliestStart(projectModel, workItemId, scheduledEndByWorkItem)
                        + item.duration().durationMillis());
                markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
                continue;
            }

            // Find the earliest start time based on the project start date and predecessor end times
            long earliestStart = earliestStart(projectModel, workItemId, scheduledEndByWorkItem);
            
            // Find a capacity window on the assignee's schedule that fits the item's duration
            ScheduleWindow window = findCapacityWindow(
                    assigneeId,
                    earliestStart,
                    item.duration().durationMillis(),
                    projectModel.planningEnd(),
                    slotsByAssignee.getOrDefault(assigneeId, List.of()),
                    reservationsBySlot
            );
            
            // Collect scheduling justifications and warning validations
            List<OptimizationConstraintViolation> violations = new ArrayList<>();
            List<String> reasons = new ArrayList<>();
            reasons.add("Scheduled at earliest available assignee capacity");
            reasons.addAll(capacityReasons(projectModel, assigneeId));
            if (earliestStart > projectModel.planningStart()) {
                reasons.add("Start delayed by dependency or prior capacity usage");
            }
            
            // Check if the scheduled task exceeds its due date
            WorkItemEntity workItem = item.workItem();
            if (workItem.getDueDate() != null && window.plannedEnd() > workItem.getDueDate()) {
                OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                        OptimizationWarningCode.LATE_RISK,
                        workItemId,
                        "Suggested plan finishes after due date",
                        "lateByMillis=" + (window.plannedEnd() - workItem.getDueDate())
                );
                warnings.add(violation);
                violations.add(violation);
            }
            
            // Check if the scheduled task exceeds the overall project planning horizon
            if (window.plannedEnd() > projectModel.planningEnd()) {
                OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                        OptimizationWarningCode.OVER_CAPACITY,
                        workItemId,
                        "Suggested plan exceeds planning horizon",
                        "plannedEnd=" + window.plannedEnd()
                );
                warnings.add(violation);
                violations.add(violation);
            }
            
            // Determine confidence levels based on constraint violations
            OptimizationConfidence confidence = confidenceFor(item, violations);
            
            // Create and store the schedule suggestion
            schedules.put(workItemId, new OptimizationScheduleSuggestion(
                    workItemId,
                    assigneeId,
                    window.plannedStart(),
                    window.plannedEnd(),
                    item.duration().durationMillis(),
                    window.allocations(),
                    confidence,
                    reasons,
                    violations));
            
            // Record the scheduled end time and update dependency constraints for successor tasks
            scheduledEndByWorkItem.put(workItemId, window.plannedEnd());
            markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
        }
        return schedules;
    }

    /**
     * Determines additional reasons regarding capacity source (e.g., fallback calendars or deducted workloads).
     *
     * @param projectModel the project model containing capacity details
     * @param assigneeId   the ID of the resource being analyzed
     * @return a list of human-readable reasons explaining the capacity source details
     */
    private List<String> capacityReasons(OptimizationProjectModel projectModel, Long assigneeId) {
        List<String> reasons = new ArrayList<>();
        var resolution = projectModel.capacityResolution();
        
        // Check if fallback calendar was used for this assignee
        if (resolution.fallbackUserIds().contains(assigneeId)) {
            reasons.add("Fallback calendar capacity used for assignee");
        }
        
        // Sum up existing plans' reserved time to see if assignee workload was already deducted
        long deductedMillis = resolution.workloadBuckets().stream()
                .filter(bucket -> Objects.equals(bucket.assigneeId(), assigneeId))
                .mapToLong(bucket -> bucket.totalReservedMillis() == null ? 0L : bucket.totalReservedMillis())
                .sum();
        if (deductedMillis > 0) {
            reasons.add("Existing work_item_plans workload deducted before scheduling");
        }
        return reasons;
    }

    /**
     * Initializes the priority queue with work items that have zero initial dependencies.
     * Ready tasks are ordered by the selected scheduling priority strategy.
     *
     * @param projectModel               the project model containing the dependency graph
     * @param itemById                   a lookup map of work items by ID
     * @param schedulingPriorityStrategy the strategy used to compare and prioritize work items
     * @return a priority queue populated with the initial set of ready work item IDs
     */
    private Queue<Long> readyQueue(OptimizationProjectModel projectModel,
                                   Map<Long, OptimizationWorkItem> itemById,
                                   OptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        PriorityQueue<Long> ready = new PriorityQueue<>(schedulingPriorityStrategy.readyWorkItemComparator(itemById));
        
        // Select work items that have empty predecessor sets in the dependency graph
        projectModel.dependencyGraph().predecessorsByWorkItemId().entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .filter(itemById::containsKey)
                .forEach(ready::add);
        return ready;
    }

    /**
     * Counts how many predecessor tasks are remaining before a work item can be scheduled.
     * This count is used to determine when a task is unlocked/ready.
     *
     * @param projectModel the project model containing the dependency graph
     * @param workItemIds  the set of all work item IDs to build the counter map for
     * @return a map of work item ID to the number of unscheduled predecessor tasks
     */
    private Map<Long, Integer> remainingPredecessors(OptimizationProjectModel projectModel, Set<Long> workItemIds) {
        Map<Long, Integer> remaining = new HashMap<>();
        for (Long workItemId : workItemIds) {
            remaining.put(workItemId, projectModel.dependencyGraph().predecessorsByWorkItemId()
                    .getOrDefault(workItemId, Set.of()).size());
        }
        return remaining;
    }

    /**
     * Decrements the predecessor count for successor tasks and inserts newly ready tasks into the queue.
     *
     * @param workItemId            the ID of the task that has just been scheduled
     * @param projectModel          the project model containing the dependency graph
     * @param remainingPredecessors the map tracking remaining predecessor counts
     * @param ready                 the queue of ready tasks to append unlocked tasks to
     */
    private void markSuccessorsReady(Long workItemId,
                                     OptimizationProjectModel projectModel,
                                     Map<Long, Integer> remainingPredecessors,
                                     Queue<Long> ready) {
        for (Long successor : projectModel.dependencyGraph().successorsByWorkItemId().getOrDefault(workItemId, Set.of())) {
            if (!remainingPredecessors.containsKey(successor)) {
                continue;
            }
            // Decrement the predecessor count since this work item is now scheduled
            int remaining = remainingPredecessors.get(successor) - 1;
            remainingPredecessors.put(successor, remaining);
            
            // If all predecessors are scheduled, the successor is now ready to be scheduled
            if (remaining == 0) {
                ready.add(successor);
            }
        }
    }

    /**
     * Resolves the assignee ID for a work item, preferring suggested assignments over original ones.
     *
     * @param item        the work item details
     * @param assignments the suggestions map for resource assignments
     * @return the resolved assignee ID, or null if no assignee is assigned
     */
    private Long resolveScheduleAssignee(OptimizationWorkItem item, Map<Long, OptimizationAssignmentSuggestion> assignments) {
        OptimizationAssignmentSuggestion assignment = assignments.get(item.workItem().getId());
        if (assignment != null) {
            return assignment.suggestedAssigneeId();
        }
        return item.workItem().getAssigneeId();
    }

    /**
     * Calculates the earliest start time for a work item. The start time must be:
     * 1. At or after the project planning start time.
     * 2. At or after the work item's earliest allowable start date.
     * 3. At or after the scheduled end times of all its predecessor tasks.
     *
     * @param projectModel           the project model containing project configurations
     * @param workItemId             the ID of the work item
     * @param scheduledEndByWorkItem the map tracking end times of scheduled tasks
     * @return the earliest timestamp (milliseconds) at which this task can start
     */
    private long earliestStart(OptimizationProjectModel projectModel,
                               Long workItemId,
                               Map<Long, Long> scheduledEndByWorkItem) {
        // Base start time is the maximum of project planning start and the work item's earliest start constraints
        long earliest = Math.max(projectModel.planningStart(),
                projectModel.earliestStartByWorkItemId().getOrDefault(workItemId, projectModel.planningStart()));
        
        // Elevate the start time if any predecessor task ends after the base start time
        for (Long predecessor : projectModel.dependencyGraph().predecessorsByWorkItemId().getOrDefault(workItemId, Set.of())) {
            earliest = Math.max(earliest, scheduledEndByWorkItem.getOrDefault(predecessor, projectModel.planningStart()));
        }
        return earliest;
    }

    /**
     * Greedy interval-packing algorithm to find the earliest capacity window for an assignee.
     * It iterates through sorted capacity slots, skips over past slots, handles existing reservations,
     * fits chunks of the work item duration, and handles any overflow beyond the planning horizon.
     *
     * @param assigneeId          the resource assignee ID
     * @param earliestStart       the earliest possible start time based on dependencies
     * @param durationMillis      the total remaining duration of the work item to schedule (in milliseconds)
     * @param planningEnd         the end of the planning horizon
     * @param slots               the available capacity slots for the assignee
     * @param reservationsBySlot  the map of already reserved time ranges per capacity slot
     * @return a ScheduleWindow containing the planned start, end, and breakdown of allocations
     */
    private ScheduleWindow findCapacityWindow(Long assigneeId,
                                              long earliestStart,
                                              long durationMillis,
                                              long planningEnd,
                                              List<ResourceCapacitySlot> slots,
                                              Map<SlotKey, List<TimeRange>> reservationsBySlot) {
        long remaining = durationMillis;
        Long plannedStart = null;
        long plannedEnd = earliestStart;
        List<OptimizationScheduleAllocation> allocations = new ArrayList<>();

        for (ResourceCapacitySlot slot : slots) {
            // Stop searching if the task is already fully scheduled
            if (remaining <= 0) {
                break;
            }
            // Skip slots that end before our earliest allowable start time
            if (slot.slotEnd() <= earliestStart) {
                continue;
            }
            // Capacity limit of the slot determines its actual usable end time
            long slotCapacityEnd = Math.min(slot.slotEnd(), slot.slotStart() + slot.capacityMillis());
            long cursor = Math.max(slot.slotStart(), earliestStart);
            if (cursor >= slotCapacityEnd) {
                continue;
            }

            SlotKey key = new SlotKey(assigneeId, slot.slotStart(), slot.slotEnd());
            
            // Retrieve and sort existing reservations within this capacity slot
            List<TimeRange> reservations = reservationsBySlot.getOrDefault(key, List.of()).stream()
                    .sorted(Comparator.comparing(TimeRange::start))
                    .toList();
            List<TimeRange> newReservations = new ArrayList<>();
            
            // Walk through existing reservations to find empty gaps to schedule chunks of our work
            for (TimeRange reservation : reservations) {
                if (remaining <= 0) {
                    break;
                }
                if (reservation.end() <= cursor) {
                    continue;
                }
                // If there's an available gap before the next reservation starts
                if (reservation.start() > cursor) {
                    long chunkEnd = allocateChunk(cursor, Math.min(reservation.start(), slotCapacityEnd), remaining,
                            assigneeId, newReservations, allocations);
                    if (chunkEnd > cursor) {
                        if (plannedStart == null) {
                            plannedStart = cursor;
                        }
                        plannedEnd = chunkEnd;
                        remaining -= chunkEnd - cursor;
                        cursor = chunkEnd;
                    }
                }
                // Skip past the reservation window
                cursor = Math.max(cursor, reservation.end());
                if (cursor >= slotCapacityEnd) {
                    break;
                }
            }
            
            // Allocate any remaining time at the end of the slot if capacity allows
            if (remaining > 0 && cursor < slotCapacityEnd) {
                long chunkEnd = allocateChunk(cursor, slotCapacityEnd, remaining, assigneeId, newReservations, allocations);
                if (chunkEnd > cursor) {
                    if (plannedStart == null) {
                        plannedStart = cursor;
                    }
                    plannedEnd = chunkEnd;
                    remaining -= chunkEnd - cursor;
                }
            }
            
            // Update the slot's reservations list if new chunks were scheduled
            if (!newReservations.isEmpty()) {
                List<TimeRange> updated = new ArrayList<>(reservationsBySlot.getOrDefault(key, List.of()));
                updated.addAll(newReservations);
                updated.sort(Comparator.comparing(TimeRange::start));
                reservationsBySlot.put(key, updated);
            }
        }

        // Default start time if no capacity slot was found
        if (plannedStart == null) {
            plannedStart = earliestStart;
        }
        
        // Handle overflow: If there is still remaining duration after checking all slots,
        // allocate the rest immediately after the slot ends (or planningEnd) as an overflow allocation.
        if (remaining > 0) {
            long overflowStart = Math.max(plannedEnd, Math.max(earliestStart, planningEnd));
            plannedEnd = overflowStart + remaining;
            allocations.add(new OptimizationScheduleAllocation(assigneeId, overflowStart, plannedEnd, remaining));
        }
        return new ScheduleWindow(plannedStart, plannedEnd, allocations);
    }

    /**
     * Allocates a contiguous chunk of time inside an interval slot, and records it as a reservation.
     *
     * @param start           the start timestamp of the allocation gap
     * @param end             the end timestamp of the allocation gap
     * @param remaining       the remaining task duration left to schedule
     * @param assigneeId      the ID of the assignee
     * @param newReservations list to add the new reservation time range
     * @param allocations     list to add the new schedule allocation record
     * @return the end timestamp of the scheduled chunk
     */
    private long allocateChunk(long start,
                               long end,
                               long remaining,
                               Long assigneeId,
                               List<TimeRange> newReservations,
                               List<OptimizationScheduleAllocation> allocations) {
        long available = Math.max(0L, end - start);
        if (available == 0) {
            return start;
        }
        long chunk = Math.min(remaining, available);
        long chunkEnd = start + chunk;
        newReservations.add(new TimeRange(start, chunkEnd));
        allocations.add(new OptimizationScheduleAllocation(assigneeId, start, chunkEnd, chunk));
        return chunkEnd;
    }

    /**
     * Determines the confidence level of a work item plan recommendation.
     * Returns LOW if there are constraint violations (like Late Risk or Over Capacity).
     * Otherwise, inherits the duration confidence level.
     *
     * @param item       the work item being evaluated
     * @param violations list of constraint violations generated for this item
     * @return the resolved OptimizationConfidence level
     */
    private OptimizationConfidence confidenceFor(OptimizationWorkItem item, List<OptimizationConstraintViolation> violations) {
        if (!violations.isEmpty()) {
            return OptimizationConfidence.LOW;
        }
        return item.duration().confidence();
    }

    /**
     * Key representing an assignee's specific capacity slot.
     */
    private record SlotKey(Long assigneeId, Long slotStart, Long slotEnd) {
    }

    /**
     * Represents a scheduled or reserved time range.
     */
    private record TimeRange(long start, long end) {
    }

    /**
     * Represents the scheduled start, end, and individual allocations for a work item.
     */
    private record ScheduleWindow(Long plannedStart, Long plannedEnd, List<OptimizationScheduleAllocation> allocations) {
    }
}
