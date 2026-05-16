/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationConfidence;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationCandidateAssignee;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.service.IOptimizationRunGenerator;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GreedyOptimizationRunGenerator implements IOptimizationRunGenerator {
    // Number of milliseconds in one hour, used for overload cost calculation
    private static final long HOUR_MILLIS = 3_600_000L;

    @Override
    public OptimizationGenerationResult generate(OptimizationProjectModel projectModel, OptimizationBuilderInput input) {
        // Carry forward any pre-existing warnings from the project model
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(projectModel.warnings());
        // Step 1: Generate assignment suggestions for each work item
        Map<Long, OptimizationAssignmentSuggestion> assignments = generateAssignments(projectModel, input, warnings);
        // Step 2: Generate schedule suggestions based on assignments and dependencies
        Map<Long, OptimizationScheduleSuggestion> schedules = generateSchedules(projectModel, input, assignments, warnings);
        // Step 3: Build summary statistics for the optimization run
        OptimizationRunSummary summary = buildSummary(projectModel, assignments, schedules, warnings);
        return new OptimizationGenerationResult(assignments, schedules, warnings, summary);
    }

    private Map<Long, OptimizationAssignmentSuggestion> generateAssignments(OptimizationProjectModel projectModel,
                                                                            OptimizationBuilderInput input,
                                                                            List<OptimizationConstraintViolation> warnings) {
        Map<Long, OptimizationAssignmentSuggestion> suggestions = new LinkedHashMap<>();
        // Tracks the total duration already assigned to each assignee during this optimization run
        Map<Long, Long> assignedLoadByAssignee = new HashMap<>();
        // Pre-compute total available capacity per assignee from their capacity slots
        Map<Long, Long> capacityByAssignee = totalCapacityByAssignee(projectModel.capacitySlots());

        // Reassignment is only allowed when explicitly enabled and mode is not SCHEDULE_ONLY
        boolean assignmentEnabled = Boolean.TRUE.equals(input.allowReassignment())
                && input.mode() != OptimizationMode.SCHEDULE_ONLY;

        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemEntity workItem = item.workItem();
            // Skip done items - keep their current assignment without optimization
            if (item.done()) {
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Done item excluded from assignment optimization"));
                continue;
            }
            // When reassignment is disabled, preserve current assignments and track their load for scheduling
            if (!assignmentEnabled) {
                Long assigneeId = workItem.getAssigneeId();
                if (assigneeId != null) {
                    assignedLoadByAssignee.merge(assigneeId, item.duration().durationMillis(), Long::sum);
                }
                suggestions.put(workItem.getId(), keepCurrentAssignment(item, "Reassignment is disabled"));
                continue;
            }

            // Greedily select the best candidate based on cost function
            OptimizationAssignmentSuggestion suggestion = chooseCandidate(item, input, assignedLoadByAssignee, capacityByAssignee, warnings);
            if (suggestion.suggestedAssigneeId() != null) {
                assignedLoadByAssignee.merge(suggestion.suggestedAssigneeId(), item.duration().durationMillis(), Long::sum);
            }
            suggestions.put(workItem.getId(), suggestion);
        }
        return suggestions;
    }

    private OptimizationAssignmentSuggestion chooseCandidate(OptimizationWorkItem item,
                                                             OptimizationBuilderInput input,
                                                             Map<Long, Long> assignedLoadByAssignee,
                                                             Map<Long, Long> capacityByAssignee,
                                                             List<OptimizationConstraintViolation> warnings) {
        WorkItemEntity workItem = item.workItem();
        // No eligible candidates - fall back to current assignee with a warning
        if (item.candidateAssignees().isEmpty()) {
            OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                    OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE,
                    workItem.getId(),
                    "No eligible assignee for assignment optimization",
                    null
            );
            warnings.add(violation);
            return new OptimizationAssignmentSuggestion(workItem.getId(), workItem.getAssigneeId(), 0D,
                    List.of("No eligible assignee; current assignment kept"), List.of(violation));
        }

        // Compute cost for each candidate and sort by effective cost (ascending), then by candidate ID and work item ID for determinism
        List<CandidateCost> costs = item.candidateAssignees().stream()
                .map(candidate -> candidateCost(candidate, item, input, assignedLoadByAssignee, capacityByAssignee))
                .sorted(Comparator.comparingDouble(CandidateCost::effectiveCost)
                        .thenComparing(cost -> cost.candidate().candidateId())
                        .thenComparing(cost -> item.workItem().getId()))
                .toList();
        CandidateCost chosen = costs.get(0);
        // Detect if every candidate would exceed their available capacity
        boolean everyCandidateOverloaded = costs.stream().allMatch(CandidateCost::overloaded);
        List<OptimizationConstraintViolation> violations = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        reasons.add("Selected lowest effective assignment cost");
        // Document special role-based reasons for the selection
        if (chosen.candidate().currentAssignee()) {
            reasons.add("Current assignee retained");
        }
        if (chosen.candidate().projectLead()) {
            reasons.add("Project lead is an eligible candidate");
        }
        if (chosen.candidate().reporter()) {
            reasons.add("Reporter is an eligible candidate");
        }
        // If all candidates are overloaded, record a warning but still pick the least-bad option
        if (everyCandidateOverloaded) {
            OptimizationConstraintViolation violation = new OptimizationConstraintViolation(
                    OptimizationWarningCode.OVER_CAPACITY,
                    workItem.getId(),
                    "All eligible assignees exceed available planning capacity",
                    "assigneeId=" + chosen.candidate().candidateId()
            );
            warnings.add(violation);
            violations.add(violation);
            reasons.add("Overload unavoidable; selected least-bad candidate");
        }
        return new OptimizationAssignmentSuggestion(workItem.getId(), chosen.candidate().candidateId(), chosen.effectiveCost(), reasons, violations);
    }

    private CandidateCost candidateCost(OptimizationCandidateAssignee candidate,
                                        OptimizationWorkItem item,
                                        OptimizationBuilderInput input,
                                        Map<Long, Long> assignedLoadByAssignee,
                                        Map<Long, Long> capacityByAssignee) {
        // Calculate projected total load if this candidate is assigned the current work item
        long projectedLoad = assignedLoadByAssignee.getOrDefault(candidate.candidateId(), 0L) + item.duration().durationMillis();
        long capacity = capacityByAssignee.getOrDefault(candidate.candidateId(), 0L);
        long overload = Math.max(0L, projectedLoad - capacity);
        double cost = candidate.baseCost();
        // Add reassignment penalty: higher in MINIMAL_REASSIGNMENT mode to discourage changing assignees
        if (!Objects.equals(candidate.candidateId(), item.workItem().getAssigneeId())) {
            cost += input.mode() == OptimizationMode.MINIMAL_REASSIGNMENT ? 8D : 2D;
        }
        // Bonus for keeping current assignee in MINIMAL_REASSIGNMENT mode
        if (candidate.currentAssignee() && input.mode() == OptimizationMode.MINIMAL_REASSIGNMENT) {
            cost -= 5D;
        }
        // Heavy penalty for overloading: fixed cost plus proportional cost based on overload hours
        if (overload > 0) {
            cost += 25D + ((double) overload / HOUR_MILLIS);
        }
        return new CandidateCost(candidate, cost, overload > 0);
    }

    private OptimizationAssignmentSuggestion keepCurrentAssignment(OptimizationWorkItem item, String reason) {
        Long assigneeId = item.workItem().getAssigneeId();
        return new OptimizationAssignmentSuggestion(item.workItem().getId(), assigneeId, 0D, List.of(reason), List.of());
    }

    private Map<Long, OptimizationScheduleSuggestion> generateSchedules(OptimizationProjectModel projectModel,
                                                                        OptimizationBuilderInput input,
                                                                        Map<Long, OptimizationAssignmentSuggestion> assignments,
                                                                        List<OptimizationConstraintViolation> warnings) {
        // Skip scheduling if changes are disabled or mode restricts to assignment-only
        if (!Boolean.TRUE.equals(input.allowScheduleChanges()) || input.mode() == OptimizationMode.ASSIGNMENT_ONLY) {
            return Map.of();
        }
        // Cannot produce a valid schedule when dependency cycles exist
        if (projectModel.dependencyGraph().hasCycles()) {
            return Map.of();
        }

        // Index work items by ID for quick lookup during topological traversal
        Map<Long, OptimizationWorkItem> itemById = projectModel.workItems().stream()
                .collect(Collectors.toMap(item -> item.workItem().getId(), item -> item));
        // Group capacity slots by assignee and sort each group by start time
        Map<Long, List<ResourceCapacitySlot>> slotsByAssignee = projectModel.capacitySlots().stream()
                .collect(Collectors.groupingBy(ResourceCapacitySlot::assigneeId));
        slotsByAssignee.values().forEach(slots -> slots.sort(Comparator.comparing(ResourceCapacitySlot::slotStart)));

        // Track how much capacity has been consumed in each slot
        Map<SlotKey, Long> usedBySlot = new HashMap<>();
        // Track the scheduled end time of each work item to enforce dependency ordering
        Map<Long, Long> scheduledEndByWorkItem = new HashMap<>();
        Map<Long, OptimizationScheduleSuggestion> schedules = new LinkedHashMap<>();
        // Initialize the priority queue with items that have no predecessors
        Queue<Long> ready = readyQueue(projectModel, itemById);
        // Track how many predecessors remain unscheduled for each work item
        Map<Long, Integer> remainingPredecessors = remainingPredecessors(projectModel, itemById.keySet());

        // Topological traversal: process items in dependency order, prioritized by critical path and other factors
        while (!ready.isEmpty()) {
            Long workItemId = ready.poll();
            OptimizationWorkItem item = itemById.get(workItemId);
            // Skip null or done items, but still unblock their successors
            if (item == null || item.done()) {
                markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
                continue;
            }
            // Resolve which assignee to use for scheduling (from assignment suggestion or current assignment)
            Long assigneeId = resolveScheduleAssignee(item, assignments);
            if (assigneeId == null) {
                // No assignee available - skip scheduling but record a phantom end time for dependency purposes
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

            // Find the earliest time the assignee has enough capacity to complete this work item
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
            if (earliestStart > projectModel.planningStart()) {
                reasons.add("Start delayed by dependency or prior capacity usage");
            }
            WorkItemEntity workItem = item.workItem();
            // Check if the scheduled end exceeds the work item's due date
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
            // Check if the scheduled end exceeds the overall planning horizon
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
            // Confidence is reduced when constraint violations are present
            OptimizationConfidence confidence = confidenceFor(item, violations);
            schedules.put(workItemId, new OptimizationScheduleSuggestion(workItemId, assigneeId, window.plannedStart(), window.plannedEnd(),
                    confidence, reasons, violations));
            scheduledEndByWorkItem.put(workItemId, window.plannedEnd());
            // Unblock successor items now that this item is scheduled
            markSuccessorsReady(workItemId, projectModel, remainingPredecessors, ready);
        }
        return schedules;
    }

    private Queue<Long> readyQueue(OptimizationProjectModel projectModel, Map<Long, OptimizationWorkItem> itemById) {
        // Priority queue orders items by critical path, priority score, due date, duration, rank, and ID
        PriorityQueue<Long> ready = new PriorityQueue<>(scheduleComparator(itemById));
        // Seed the queue with items that have no predecessors (root nodes in the dependency graph)
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
        // Decrement the predecessor count for each successor; add to ready queue when all predecessors are done
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
        // Multi-key comparator for scheduling priority:
        // 1. Critical path items first (highest priority)
        // 2. Higher priority score first
        // 3. Earlier due date first (nulls last)
        // 4. Shorter duration first
        // 5. Lower rank first (nulls last)
        // 6. Lower work item ID as final tiebreaker
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
        // Start no earlier than the global planning start and the pre-computed earliest start
        long earliest = Math.max(projectModel.planningStart(), projectModel.earliestStartByWorkItemId().getOrDefault(workItemId, projectModel.planningStart()));
        // Also respect the scheduled end of all predecessor work items
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

        // Iterate through sorted capacity slots to find enough available time
        for (ResourceCapacitySlot slot : slots) {
            if (remaining <= 0) {
                break;
            }
            // Skip slots that end before the earliest allowed start
            if (slot.slotEnd() <= earliestStart) {
                continue;
            }
            SlotKey key = new SlotKey(assigneeId, slot.slotStart());
            long used = usedBySlot.getOrDefault(key, 0L);
            long available = Math.max(0L, slot.capacityMillis() - used);
            if (available == 0) {
                continue;
            }
            // Allocation starts after any already-used portion of this slot, respecting the earliest start constraint
            long allocationStart = Math.max(slot.slotStart() + used, earliestStart);
            long chunk = Math.min(remaining, available);
            if (plannedStart == null) {
                plannedStart = allocationStart;
            }
            plannedEnd = allocationStart + chunk;
            usedBySlot.put(key, used + chunk);
            remaining -= chunk;
        }

        // If no capacity was found, fall back to starting at earliestStart
        if (plannedStart == null) {
            plannedStart = earliestStart;
        }
        // If work could not fit in available slots, extend beyond the last slot or planning horizon
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

    private OptimizationRunSummary buildSummary(OptimizationProjectModel projectModel,
                                                Map<Long, OptimizationAssignmentSuggestion> assignments,
                                                Map<Long, OptimizationScheduleSuggestion> schedules,
                                                List<OptimizationConstraintViolation> warnings) {
        Set<Long> assignees = new HashSet<>();
        int assignmentSuggestionCount = 0;
        // Count unique assignees and detect how many items would change assignee
        for (OptimizationWorkItem item : projectModel.workItems()) {
            Long currentAssignee = item.workItem().getAssigneeId();
            OptimizationAssignmentSuggestion suggestion = assignments.get(item.workItem().getId());
            Long suggestedAssignee = suggestion == null ? currentAssignee : suggestion.suggestedAssigneeId();
            if (suggestedAssignee != null) {
                assignees.add(suggestedAssignee);
            }
            if (!Objects.equals(currentAssignee, suggestedAssignee)) {
                assignmentSuggestionCount++;
            }
        }
        return OptimizationRunSummary.builder()
                .scopeSize(projectModel.workItems().size())
                .assigneeCount(assignees.size())
                .dependencyCount(projectModel.dependencyGraph().internalEdges().size())
                .planningStart(projectModel.planningStart())
                .planningEnd(projectModel.planningEnd())
                .assignmentSuggestionCount(assignmentSuggestionCount)
                .scheduledItemCount(schedules.size())
                .lateItemsBefore(lateItemsBefore(projectModel))
                .lateItemsAfter(lateItemsAfter(projectModel, schedules))
                // Before count is 0 because overload is computed against the optimized schedule only
                .overloadedAssigneeCountBefore(0)
                .overloadedAssigneeCountAfter(overloadedAssigneeCount(projectModel, schedules))
                .warningsCount(warnings.size())
                .confidenceLevel(confidenceLevel(warnings))
                .build();
    }

    private int lateItemsBefore(OptimizationProjectModel projectModel) {
        // Count items whose current active plan ends after their due date
        int count = 0;
        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemPlanEntity plan = item.activePlan();
            Long dueDate = item.workItem().getDueDate();
            if (plan != null && dueDate != null && plan.getPlannedEnd() != null && plan.getPlannedEnd() > dueDate) {
                count++;
            }
        }
        return count;
    }

    private int lateItemsAfter(OptimizationProjectModel projectModel, Map<Long, OptimizationScheduleSuggestion> schedules) {
        // Count items whose optimized schedule ends after their due date
        int count = 0;
        for (OptimizationWorkItem item : projectModel.workItems()) {
            OptimizationScheduleSuggestion schedule = schedules.get(item.workItem().getId());
            Long dueDate = item.workItem().getDueDate();
            if (schedule != null && dueDate != null && schedule.plannedEnd() > dueDate) {
                count++;
            }
        }
        return count;
    }

    private int overloadedAssigneeCount(OptimizationProjectModel projectModel, Map<Long, OptimizationScheduleSuggestion> schedules) {
        // Sum scheduled duration per assignee and compare against their total capacity
        Map<Long, Long> scheduledByAssignee = new HashMap<>();
        schedules.values().forEach(schedule -> scheduledByAssignee.merge(
                schedule.assigneeId(),
                schedule.plannedEnd() - schedule.plannedStart(),
                Long::sum
        ));
        Map<Long, Long> capacityByAssignee = totalCapacityByAssignee(projectModel.capacitySlots());
        return (int) scheduledByAssignee.entrySet().stream()
                .filter(entry -> entry.getValue() > capacityByAssignee.getOrDefault(entry.getKey(), 0L))
                .count();
    }

    private String confidenceLevel(List<OptimizationConstraintViolation> warnings) {
        // LOW confidence if critical warnings exist (cycles, over-capacity, no eligible assignee)
        boolean low = warnings.stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.DEPENDENCY_CYCLE
                || warning.code() == OptimizationWarningCode.OVER_CAPACITY
                || warning.code() == OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE);
        if (low) {
            return OptimizationConfidence.LOW.name();
        }
        // HIGH if no warnings at all, MEDIUM if only non-critical warnings exist
        return warnings.isEmpty() ? OptimizationConfidence.HIGH.name() : OptimizationConfidence.MEDIUM.name();
    }

    private Map<Long, Long> totalCapacityByAssignee(List<ResourceCapacitySlot> slots) {
        return slots.stream().collect(Collectors.groupingBy(
                ResourceCapacitySlot::assigneeId,
                Collectors.summingLong(ResourceCapacitySlot::capacityMillis)
        ));
    }

    private record CandidateCost(OptimizationCandidateAssignee candidate, double effectiveCost, boolean overloaded) {
    }

    private record SlotKey(Long assigneeId, Long slotStart) {
    }

    private record ScheduleWindow(Long plannedStart, Long plannedEnd) {
    }
}
