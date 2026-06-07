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

@Service
public class GreedySchedulingPolicy implements OptimizationSchedulingPolicy {
    @Override
    public Map<Long, OptimizationScheduleSuggestion> generateSchedules(
            OptimizationProjectModel projectModel,
            OptimizationAlgorithmOptions options,
            Map<Long, OptimizationAssignmentSuggestion> assignments,
            List<OptimizationConstraintViolation> warnings) {
        if (!options.intent().changeScope().includesScheduling()) {
            return Map.of();
        }
        if (projectModel.dependencyGraph().hasCycles()) {
            return Map.of();
        }

        Map<Long, OptimizationWorkItem> itemById = projectModel.workItems().stream()
                .collect(Collectors.toMap(item -> item.workItem().getId(), item -> item));
        Map<Long, List<ResourceCapacitySlot>> slotsByAssignee = projectModel.capacitySlots().stream()
                .collect(Collectors.groupingBy(ResourceCapacitySlot::assigneeId));
        slotsByAssignee.values().forEach(slots -> slots.sort(Comparator.comparing(ResourceCapacitySlot::slotStart)));

        Map<SlotKey, List<TimeRange>> reservationsBySlot = new HashMap<>();
        Map<Long, Long> scheduledEndByWorkItem = new HashMap<>();
        Map<Long, OptimizationScheduleSuggestion> schedules = new LinkedHashMap<>();
        Queue<Long> ready = readyQueue(projectModel, itemById, options.schedulingPriorityStrategy());
        Map<Long, Integer> remainingPredecessors = remainingPredecessors(projectModel, itemById.keySet());

        while (!ready.isEmpty()) {
            Long workItemId = ready.poll();
            OptimizationWorkItem item = itemById.get(workItemId);
            if (item == null || item.done()) {
                markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
                continue;
            }
            Long assigneeId = resolveScheduleAssignee(item, assignments);
            if (assigneeId == null) {
                OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                        OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE,
                        workItemId,
                        "Schedule skipped because no assignee is available",
                        null
                );
                warnings.add(violation);
                scheduledEndByWorkItem.put(workItemId, earliestStart(projectModel, workItemId, scheduledEndByWorkItem)
                        + item.duration().durationMillis());
                markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
                continue;
            }

            long earliestStart = earliestStart(projectModel, workItemId, scheduledEndByWorkItem);
            ScheduleWindow window = findCapacityWindow(
                    assigneeId,
                    earliestStart,
                    item.duration().durationMillis(),
                    projectModel.planningEnd(),
                    slotsByAssignee.getOrDefault(assigneeId, List.of()),
                    reservationsBySlot
            );
            List<OptimizationConstraintViolation> violations = new ArrayList<>();
            List<String> reasons = new ArrayList<>();
            reasons.add("Scheduled at earliest available assignee capacity");
            reasons.addAll(capacityReasons(projectModel, assigneeId));
            if (earliestStart > projectModel.planningStart()) {
                reasons.add("Start delayed by dependency or prior capacity usage");
            }
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
            OptimizationConfidence confidence = confidenceFor(item, violations);
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
            scheduledEndByWorkItem.put(workItemId, window.plannedEnd());
            markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
        }
        return schedules;
    }

    private List<String> capacityReasons(OptimizationProjectModel projectModel, Long assigneeId) {
        List<String> reasons = new ArrayList<>();
        var resolution = projectModel.capacityResolution();
        if (resolution.fallbackUserIds().contains(assigneeId)) {
            reasons.add("Fallback calendar capacity used for assignee");
        }
        long deductedMillis = resolution.workloadBuckets().stream()
                .filter(bucket -> Objects.equals(bucket.assigneeId(), assigneeId))
                .mapToLong(bucket -> bucket.totalReservedMillis() == null ? 0L : bucket.totalReservedMillis())
                .sum();
        if (deductedMillis > 0) {
            reasons.add("Existing work_item_plans workload deducted before scheduling");
        }
        return reasons;
    }

    private Queue<Long> readyQueue(OptimizationProjectModel projectModel,
                                   Map<Long, OptimizationWorkItem> itemById,
                                   OptimizationSchedulingPriorityStrategy schedulingPriorityStrategy) {
        PriorityQueue<Long> ready = new PriorityQueue<>(schedulingPriorityStrategy.readyWorkItemComparator(itemById));
        projectModel.dependencyGraph().predecessorsByWorkItemId().entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .filter(itemById::containsKey)
                .forEach(ready::add);
        return ready;
    }

    private Map<Long, Integer> remainingPredecessors(OptimizationProjectModel projectModel, Set<Long> workItemIds) {
        Map<Long, Integer> remaining = new HashMap<>();
        for (Long workItemId : workItemIds) {
            remaining.put(workItemId, projectModel.dependencyGraph().predecessorsByWorkItemId()
                    .getOrDefault(workItemId, Set.of()).size());
        }
        return remaining;
    }

    private void markSuccessorsReady(Long workItemId,
                                     OptimizationProjectModel projectModel,
                                     Map<Long, Integer> remainingPredecessors,
                                     Queue<Long> ready) {
        for (Long successor : projectModel.dependencyGraph().successorsByWorkItemId().getOrDefault(workItemId, Set.of())) {
            if (!remainingPredecessors.containsKey(successor)) {
                continue;
            }
            int remaining = remainingPredecessors.get(successor) - 1;
            remainingPredecessors.put(successor, remaining);
            if (remaining == 0) {
                ready.add(successor);
            }
        }
    }

    private Long resolveScheduleAssignee(OptimizationWorkItem item, Map<Long, OptimizationAssignmentSuggestion> assignments) {
        OptimizationAssignmentSuggestion assignment = assignments.get(item.workItem().getId());
        if (assignment != null) {
            return assignment.suggestedAssigneeId();
        }
        return item.workItem().getAssigneeId();
    }

    private long earliestStart(OptimizationProjectModel projectModel,
                               Long workItemId,
                               Map<Long, Long> scheduledEndByWorkItem) {
        long earliest = Math.max(projectModel.planningStart(),
                projectModel.earliestStartByWorkItemId().getOrDefault(workItemId, projectModel.planningStart()));
        for (Long predecessor : projectModel.dependencyGraph().predecessorsByWorkItemId().getOrDefault(workItemId, Set.of())) {
            earliest = Math.max(earliest, scheduledEndByWorkItem.getOrDefault(predecessor, projectModel.planningStart()));
        }
        return earliest;
    }

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
            if (remaining <= 0) {
                break;
            }
            if (slot.slotEnd() <= earliestStart) {
                continue;
            }
            long slotCapacityEnd = Math.min(slot.slotEnd(), slot.slotStart() + slot.capacityMillis());
            long cursor = Math.max(slot.slotStart(), earliestStart);
            if (cursor >= slotCapacityEnd) {
                continue;
            }

            SlotKey key = new SlotKey(assigneeId, slot.slotStart(), slot.slotEnd());
            List<TimeRange> reservations = reservationsBySlot.getOrDefault(key, List.of()).stream()
                    .sorted(Comparator.comparing(TimeRange::start))
                    .toList();
            List<TimeRange> newReservations = new ArrayList<>();
            for (TimeRange reservation : reservations) {
                if (remaining <= 0) {
                    break;
                }
                if (reservation.end() <= cursor) {
                    continue;
                }
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
                cursor = Math.max(cursor, reservation.end());
                if (cursor >= slotCapacityEnd) {
                    break;
                }
            }
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
            if (!newReservations.isEmpty()) {
                List<TimeRange> updated = new ArrayList<>(reservationsBySlot.getOrDefault(key, List.of()));
                updated.addAll(newReservations);
                updated.sort(Comparator.comparing(TimeRange::start));
                reservationsBySlot.put(key, updated);
            }
        }

        if (plannedStart == null) {
            plannedStart = earliestStart;
        }
        if (remaining > 0) {
            long overflowStart = Math.max(plannedEnd, Math.max(earliestStart, planningEnd));
            plannedEnd = overflowStart + remaining;
            allocations.add(new OptimizationScheduleAllocation(assigneeId, overflowStart, plannedEnd, remaining));
        }
        return new ScheduleWindow(plannedStart, plannedEnd, allocations);
    }

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

    private OptimizationConfidence confidenceFor(OptimizationWorkItem item, List<OptimizationConstraintViolation> violations) {
        if (!violations.isEmpty()) {
            return OptimizationConfidence.LOW;
        }
        return item.duration().confidence();
    }

    private record SlotKey(Long assigneeId, Long slotStart, Long slotEnd) {
    }

    private record TimeRange(long start, long end) {
    }

    private record ScheduleWindow(Long plannedStart, Long plannedEnd, List<OptimizationScheduleAllocation> allocations) {
    }
}
