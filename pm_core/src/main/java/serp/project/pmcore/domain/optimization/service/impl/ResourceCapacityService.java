/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.CapacityWorkloadBucket;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadAllocation;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadItem;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadPlan;
import serp.project.pmcore.domain.optimization.port.IResourceWorkloadReadPort;
import serp.project.pmcore.domain.optimization.service.IResourceCalendarService;
import serp.project.pmcore.domain.optimization.service.IResourceCapacityService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceCapacityService implements IResourceCapacityService {
    private final IResourceCalendarService resourceCalendarService;
    private final IResourceWorkloadReadPort resourceWorkloadReadPort;

    @Override
    public List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                       List<Long> userIds,
                                                       Long planningStart,
                                                       Long planningEnd) {
        return resourceCalendarService.resolveWorkingCapacity(tenantId, userIds, planningStart, planningEnd).slots();
    }

    @Override
    public CapacityResolutionResult resolveCapacity(Long tenantId,
                                                    Long projectId,
                                                    List<Long> userIds,
                                                    Long planningStart,
                                                    Long planningEnd,
                                                    List<Long> excludedWorkItemIds) {
        CalendarCapacityResult calendar = resourceCalendarService.resolveWorkingCapacity(tenantId, userIds, planningStart, planningEnd);
        List<ResourceCapacitySlot> calendarSlots = calendar.slots();
        if (calendarSlots.isEmpty()) {
            return new CapacityResolutionResult(List.of(), capacitySourceMode(calendar.coverageStatus(), false),
                    calendar.coverageStatus(), CapacityCoverageStatus.NOT_REQUIRED, calendar.fallbackUserIds(), calendar.fetchedAt(),
                    System.currentTimeMillis(), 0L, 0L, 0L, List.of(), calendar.warnings());
        }

        List<Long> safeExcludedIds = excludedWorkItemIds == null || excludedWorkItemIds.isEmpty()
                ? List.of(-1L)
                : excludedWorkItemIds;
        List<ResourceWorkloadPlan> plans = resourceWorkloadReadPort.findActiveWorkloadPlans(
                tenantId,
                userIds,
                planningStart,
                planningEnd,
                safeExcludedIds
        );
        List<ResourceWorkloadItem> unplannedWorkItems = resourceWorkloadReadPort.findActiveUnplannedWorkloadItems(
                tenantId,
                userIds,
                planningStart,
                planningEnd,
                safeExcludedIds
        );
        if (plans.isEmpty() && unplannedWorkItems.isEmpty()) {
            return new CapacityResolutionResult(calendarSlots, capacitySourceMode(calendar.coverageStatus(), false),
                    calendar.coverageStatus(), CapacityCoverageStatus.FULL, calendar.fallbackUserIds(), calendar.fetchedAt(),
                    System.currentTimeMillis(), 0L, 0L, 0L, List.of(), calendar.warnings());
        }

        Map<Long, ResourceWorkloadItem> workItemsById = plans.isEmpty()
                ? Map.of()
                : resourceWorkloadReadPort.findWorkItemsByIds(
                                tenantId,
                                plans.stream().map(ResourceWorkloadPlan::workItemId).distinct().toList()
                        )
                        .stream()
                        .filter(item -> item.assigneeId() != null && userIds.contains(item.assigneeId()))
                        .collect(Collectors.toMap(ResourceWorkloadItem::id, item -> item, (left, right) -> left));
        List<Long> planIds = plans.stream()
                .map(ResourceWorkloadPlan::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<ResourceWorkloadAllocation> allocations = planIds.isEmpty()
                ? List.of()
                : resourceWorkloadReadPort.findAllocationsByPlanIds(tenantId, planIds);
        Map<Long, List<ResourceWorkloadAllocation>> allocationsByPlanId = allocations == null
                ? Map.of()
                : allocations.stream()
                        .collect(Collectors.groupingBy(ResourceWorkloadAllocation::workItemPlanId));
        Map<Long, Long> sameProjectUnplannedByAssignee = unplannedWorkItems.stream()
                .filter(item -> Objects.equals(item.projectId(), projectId))
                .collect(Collectors.groupingBy(ResourceWorkloadItem::assigneeId,
                        Collectors.summingLong(this::remainingEstimateMillis)));
        Map<Long, Long> crossProjectUnplannedByAssignee = unplannedWorkItems.stream()
                .filter(item -> !Objects.equals(item.projectId(), projectId))
                .collect(Collectors.groupingBy(ResourceWorkloadItem::assigneeId,
                        Collectors.summingLong(this::remainingEstimateMillis)));

        Map<BucketKey, BucketAccumulator> bucketAccumulators = new HashMap<>();
        List<ResourceCapacitySlot> netSlots = new ArrayList<>();
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(calendar.warnings());
        for (ResourceCapacitySlot slot : calendarSlots) {
            long sameProject = 0L;
            long crossProject = 0L;
            for (ResourceWorkloadPlan plan : plans) {
                ResourceWorkloadItem item = workItemsById.get(plan.workItemId());
                if (item == null || !Objects.equals(item.assigneeId(), slot.assigneeId())) {
                    continue;
                }
                long overlap = plannedOverlapMillis(slot, plan, allocationsByPlanId.getOrDefault(plan.id(), List.of()));
                if (overlap <= 0) {
                    continue;
                }
                if (Objects.equals(item.projectId(), projectId)) {
                    sameProject += overlap;
                } else {
                    crossProject += overlap;
                }
            }
            long remainingCapacity = Math.max(0L, slot.capacityMillis() - sameProject - crossProject);
            long unplannedSameProject = consumeWorkload(sameProjectUnplannedByAssignee, slot.assigneeId(), remainingCapacity);
            sameProject += unplannedSameProject;
            remainingCapacity = Math.max(0L, remainingCapacity - unplannedSameProject);
            crossProject += consumeWorkload(crossProjectUnplannedByAssignee, slot.assigneeId(), remainingCapacity);
            long reserved = sameProject + crossProject;
            if (reserved > slot.capacityMillis()) {
                warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.CAPACITY_RESERVATION_EXCEEDS_AVAILABILITY,
                        null, "Reserved workload exceeds fallback capacity", slot.assigneeId() + ":" + slot.slotStart()));
                if (crossProject > 0) {
                    warnings.add(new OptimizationConstraintViolation(OptimizationWarningCode.CROSS_PROJECT_CAPACITY_CONFLICT,
                            null, "Cross-project workload consumes available capacity", slot.assigneeId() + ":" + slot.slotStart()));
                }
            }
            long deducted = Math.min(slot.capacityMillis(), reserved);
            netSlots.add(new ResourceCapacitySlot(slot.assigneeId(), slot.slotStart(), slot.slotEnd(), slot.capacityMillis() - deducted));
            if (reserved > 0) {
                bucketAccumulators.computeIfAbsent(new BucketKey(slot.assigneeId(), slot.slotStart(), slot.slotEnd()), key -> new BucketAccumulator())
                        .add(sameProject, crossProject);
            }
        }
        List<CapacityWorkloadBucket> buckets = bucketAccumulators.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<BucketKey, BucketAccumulator> entry) -> entry.getKey().assigneeId())
                        .thenComparing(entry -> entry.getKey().start()))
                .map(entry -> entry.getValue().toBucket(entry.getKey()))
                .toList();
        long sameProjectMillis = buckets.stream().mapToLong(CapacityWorkloadBucket::sameProjectOutsideScopeReservedMillis).sum();
        long crossProjectMillis = buckets.stream().mapToLong(CapacityWorkloadBucket::crossProjectReservedMillis).sum();
        return new CapacityResolutionResult(netSlots, capacitySourceMode(calendar.coverageStatus(), sameProjectMillis + crossProjectMillis > 0),
                calendar.coverageStatus(), CapacityCoverageStatus.FULL, calendar.fallbackUserIds(),
                calendar.fetchedAt(), System.currentTimeMillis(), sameProjectMillis + crossProjectMillis,
                sameProjectMillis, crossProjectMillis, buckets, warnings);
    }

    private CapacitySourceMode capacitySourceMode(CapacityCoverageStatus calendarCoverageStatus, boolean workloadDeducted) {
        boolean hasRealCalendar = calendarCoverageStatus == CapacityCoverageStatus.FULL
                || calendarCoverageStatus == CapacityCoverageStatus.PARTIAL;
        if (hasRealCalendar) {
            return workloadDeducted ? CapacitySourceMode.REAL_CALENDAR_WITH_WORKLOAD : CapacitySourceMode.REAL_CALENDAR_ONLY;
        }
        return workloadDeducted ? CapacitySourceMode.FALLBACK_WITH_WORKLOAD : CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC_PLUS_7;
    }

    private long consumeWorkload(Map<Long, Long> workloadByAssignee, Long assigneeId, long capacityMillis) {
        long remainingWorkload = workloadByAssignee.getOrDefault(assigneeId, 0L);
        if (remainingWorkload <= 0 || capacityMillis <= 0) {
            return 0L;
        }
        long consumed = Math.min(remainingWorkload, capacityMillis);
        workloadByAssignee.put(assigneeId, remainingWorkload - consumed);
        return consumed;
    }

    private long remainingEstimateMillis(ResourceWorkloadItem item) {
        Long estimate = item.timeRemainingEstimate() != null
                ? item.timeRemainingEstimate()
                : item.timeOriginalEstimate();
        return estimate == null || estimate <= 0 ? 0L : Math.multiplyExact(estimate, OptimizationConstants.MINUTE_MILLIS);
    }

    private long plannedOverlapMillis(ResourceCapacitySlot slot,
                                      ResourceWorkloadPlan plan,
                                      List<ResourceWorkloadAllocation> allocations) {
        if (allocations != null && !allocations.isEmpty()) {
            return allocations.stream()
                    .filter(allocation -> Objects.equals(allocation.assigneeId(), slot.assigneeId()))
                    .mapToLong(allocation -> allocationOverlapMillis(slot, allocation))
                    .sum();
        }
        return overlapMillis(slot.slotStart(), slot.slotEnd(), plan.plannedStart(), plan.plannedEnd());
    }

    private long allocationOverlapMillis(ResourceCapacitySlot slot, ResourceWorkloadAllocation allocation) {
        long overlap = overlapMillis(slot.slotStart(), slot.slotEnd(), allocation.startTime(), allocation.endTime());
        if (overlap <= 0) {
            return 0L;
        }
        Long effortMillis = allocation.effortMillis();
        return effortMillis == null || effortMillis <= 0 ? overlap : Math.min(overlap, effortMillis);
    }

    private long overlapMillis(Long startA, Long endA, Long startB, Long endB) {
        if (startA == null || endA == null || startB == null || endB == null) {
            return 0L;
        }
        return Math.max(0L, Math.min(endA, endB) - Math.max(startA, startB));
    }

    private record BucketKey(Long assigneeId, Long start, Long end) {
    }

    private static class BucketAccumulator {
        private long sameProject;
        private long crossProject;

        private void add(long sameProject, long crossProject) {
            this.sameProject += sameProject;
            this.crossProject += crossProject;
        }

        private CapacityWorkloadBucket toBucket(BucketKey key) {
            return new CapacityWorkloadBucket(key.assigneeId(), key.start(), key.end(), sameProject, crossProject, sameProject + crossProject);
        }
    }
}
