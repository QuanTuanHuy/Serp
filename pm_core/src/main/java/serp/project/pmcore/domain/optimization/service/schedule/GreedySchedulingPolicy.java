/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.schedule;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
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
        if (!Boolean.TRUE.equals(options.allowScheduleChanges()) || options.mode() == OptimizationMode.ASSIGNMENT_ONLY) {
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

        Map<SlotKey, Long> usedBySlot = new HashMap<>();
        Map<Long, Long> scheduledEndByWorkItem = new HashMap<>();
        Map<Long, OptimizationScheduleSuggestion> schedules = new LinkedHashMap<>();
        Queue<Long> ready = readyQueue(projectModel, itemById);
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
                    usedBySlot
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

    private Queue<Long> readyQueue(OptimizationProjectModel projectModel, Map<Long, OptimizationWorkItem> itemById) {
        PriorityQueue<Long> ready = new PriorityQueue<>(scheduleComparator(itemById));
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

    private Comparator<Long> scheduleComparator(Map<Long, OptimizationWorkItem> itemById) {
        return Comparator.<Long, Boolean>comparing(id -> itemById.get(id).criticalPath()).reversed()
                .thenComparing(id -> itemById.get(id).priorityScore().score(), Comparator.reverseOrder())
                .thenComparing(id -> itemById.get(id).workItem().getDueDate(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(id -> itemById.get(id).duration().durationMillis())
                .thenComparing(id -> itemById.get(id).workItem().getRank(), Comparator.nullsLast(String::compareTo))
                .thenComparing(id -> id);
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
                                              Map<SlotKey, Long> usedBySlot) {
        long remaining = durationMillis;
        Long plannedStart = null;
        long plannedEnd = earliestStart;

        for (ResourceCapacitySlot slot : slots) {
            if (remaining <= 0) {
                break;
            }
            if (slot.slotEnd() <= earliestStart) {
                continue;
            }
            SlotKey key = new SlotKey(assigneeId, slot.slotStart());
            long used = usedBySlot.getOrDefault(key, 0L);
            long slotAvailableStart = Math.max(slot.slotStart() + used, earliestStart);
            if (slotAvailableStart >= slot.slotEnd()) {
                continue;
            }
            long available = Math.max(0L, slot.slotEnd() - slotAvailableStart);
            long capacityRemaining = Math.max(0L, slot.capacityMillis() - used);
            available = Math.min(available, capacityRemaining);
            if (available == 0) {
                continue;
            }
            long chunk = Math.min(remaining, available);
            if (plannedStart == null) {
                plannedStart = slotAvailableStart;
            }
            plannedEnd = slotAvailableStart + chunk;
            usedBySlot.put(key, used + chunk);
            remaining -= chunk;
        }

        if (plannedStart == null) {
            plannedStart = earliestStart;
        }
        if (remaining > 0) {
            plannedEnd = Math.max(plannedEnd, Math.max(earliestStart, planningEnd)) + remaining;
        }
        return new ScheduleWindow(plannedStart, plannedEnd);
    }

    private OptimizationConfidence confidenceFor(OptimizationWorkItem item, List<OptimizationConstraintViolation> violations) {
        if (!violations.isEmpty()) {
            return OptimizationConfidence.LOW;
        }
        return item.duration().confidence();
    }

    private record SlotKey(Long assigneeId, Long slotStart) {
    }

    private record ScheduleWindow(Long plannedStart, Long plannedEnd) {
    }
}
