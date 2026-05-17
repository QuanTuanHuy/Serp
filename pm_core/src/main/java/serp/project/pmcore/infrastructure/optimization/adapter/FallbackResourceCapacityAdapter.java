/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.CapacityWorkloadBucket;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.port.IResourceCalendarPort;
import serp.project.pmcore.domain.optimization.port.IResourceCapacityPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FallbackResourceCapacityAdapter implements IResourceCapacityPort {
    private final IWorkItemPlanRepository workItemPlanRepository;
    private final IWorkItemRepository workItemRepository;
    private final WorkItemPlanMapper workItemPlanMapper;
    private final IResourceCalendarPort resourceCalendarPort;

    public FallbackResourceCapacityAdapter(IWorkItemPlanRepository workItemPlanRepository,
                                           IWorkItemRepository workItemRepository,
                                           WorkItemPlanMapper workItemPlanMapper,
                                           IResourceCalendarPort resourceCalendarPort) {
        this.workItemPlanRepository = workItemPlanRepository;
        this.workItemRepository = workItemRepository;
        this.workItemPlanMapper = workItemPlanMapper;
        this.resourceCalendarPort = resourceCalendarPort;
    }

    @Override
    public List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                       List<Long> userIds,
                                                       Long planningStart,
                                                       Long planningEnd) {
        return resourceCalendarPort.resolveWorkingCapacity(tenantId, userIds, planningStart, planningEnd).slots();
    }

    @Override
    public CapacityResolutionResult resolveCapacity(Long tenantId,
                                                    Long projectId,
                                                    List<Long> userIds,
                                                    Long planningStart,
                                                    Long planningEnd,
                                                    List<Long> excludedWorkItemIds) {
        CalendarCapacityResult calendar = resourceCalendarPort.resolveWorkingCapacity(tenantId, userIds, planningStart, planningEnd);
        List<ResourceCapacitySlot> calendarSlots = calendar.slots();
        if (calendarSlots.isEmpty()) {
            return new CapacityResolutionResult(List.of(), CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                    calendar.coverageStatus(), CapacityCoverageStatus.NOT_REQUIRED, calendar.fallbackUserIds(), calendar.fetchedAt(),
                    System.currentTimeMillis(), 0L, 0L, 0L, List.of(), calendar.warnings());
        }

        List<Long> safeExcludedIds = excludedWorkItemIds == null || excludedWorkItemIds.isEmpty()
                ? List.of(-1L)
                : excludedWorkItemIds;
        List<WorkItemPlanModel> planModels = workItemPlanRepository.findActiveWorkloadPlans(
                tenantId,
                userIds,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(planningStart), ZoneOffset.UTC),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(planningEnd), ZoneOffset.UTC),
                safeExcludedIds
        );
        if (planModels.isEmpty()) {
            return new CapacityResolutionResult(calendarSlots, CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                    calendar.coverageStatus(), CapacityCoverageStatus.FULL, calendar.fallbackUserIds(), calendar.fetchedAt(),
                    System.currentTimeMillis(), 0L, 0L, 0L, List.of(), calendar.warnings());
        }
        Map<Long, WorkItemModel> workItemsById = workItemRepository.findAllByTenantIdAndIdIn(
                        tenantId,
                        planModels.stream().map(WorkItemPlanModel::getWorkItemId).distinct().toList()
                )
                .stream()
                .filter(item -> item.getAssigneeId() != null && userIds.contains(item.getAssigneeId()))
                .filter(item -> item.getResolutionId() == null)
                .collect(Collectors.toMap(WorkItemModel::getId, item -> item, (left, right) -> left));

        Map<BucketKey, BucketAccumulator> bucketAccumulators = new HashMap<>();
        List<ResourceCapacitySlot> netSlots = new ArrayList<>();
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(calendar.warnings());
        for (ResourceCapacitySlot slot : calendarSlots) {
            long sameProject = 0L;
            long crossProject = 0L;
            for (WorkItemPlanModel plan : planModels) {
                WorkItemModel item = workItemsById.get(plan.getWorkItemId());
                if (item == null || !Objects.equals(item.getAssigneeId(), slot.assigneeId())) {
                    continue;
                }
                var planEntity = workItemPlanMapper.toEntity(plan);
                Long planStart = planEntity.getPlannedStart();
                Long planEnd = planEntity.getPlannedEnd();
                long overlap = overlapMillis(slot.slotStart(), slot.slotEnd(), planStart, planEnd);
                if (overlap <= 0) {
                    continue;
                }
                if (Objects.equals(item.getProjectId(), projectId)) {
                    sameProject += overlap;
                } else {
                    crossProject += overlap;
                }
            }
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
        return new CapacityResolutionResult(netSlots,
                sameProjectMillis + crossProjectMillis > 0 ? CapacitySourceMode.FALLBACK_WITH_WORKLOAD : CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC,
                calendar.coverageStatus(), CapacityCoverageStatus.FULL, calendar.fallbackUserIds(),
                calendar.fetchedAt(), System.currentTimeMillis(), sameProjectMillis + crossProjectMillis,
                sameProjectMillis, crossProjectMillis, buckets, warnings);
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
