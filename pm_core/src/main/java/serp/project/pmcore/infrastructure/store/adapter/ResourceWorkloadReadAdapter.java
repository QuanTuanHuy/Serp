/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadAllocation;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadItem;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadPlan;
import serp.project.pmcore.domain.optimization.port.IResourceWorkloadReadPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanAllocationMapper;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanAllocationModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanAllocationRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceWorkloadReadAdapter implements IResourceWorkloadReadPort {
    private final IWorkItemPlanRepository workItemPlanRepository;
    private final IWorkItemRepository workItemRepository;
    private final IWorkItemPlanAllocationRepository workItemPlanAllocationRepository;
    private final WorkItemPlanMapper workItemPlanMapper;
    private final WorkItemMapper workItemMapper;
    private final WorkItemPlanAllocationMapper workItemPlanAllocationMapper;

    @Override
    public List<ResourceWorkloadPlan> findActiveWorkloadPlans(Long tenantId,
                                                              List<Long> userIds,
                                                              Long planningStart,
                                                              Long planningEnd,
                                                              List<Long> excludedWorkItemIds) {
        return workItemPlanRepository.findActiveWorkloadPlans(
                        tenantId,
                        userIds,
                        toUtcLocalDateTime(planningStart),
                        toUtcLocalDateTime(planningEnd),
                        excludedWorkItemIds
                )
                .stream()
                .map(this::toPlan)
                .toList();
    }

    @Override
    public List<ResourceWorkloadItem> findActiveUnplannedWorkloadItems(Long tenantId,
                                                                       List<Long> userIds,
                                                                       Long planningStart,
                                                                       Long planningEnd,
                                                                       List<Long> excludedWorkItemIds) {
        return workItemRepository.findActiveUnplannedWorkloadItems(
                        tenantId,
                        userIds,
                        excludedWorkItemIds,
                        toUtcLocalDateTime(planningStart),
                        toUtcLocalDateTime(planningEnd)
                )
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public List<ResourceWorkloadItem> findWorkItemsByIds(Long tenantId, List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }
        return workItemRepository.findAllByTenantIdAndIdIn(tenantId, workItemIds)
                .stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public List<ResourceWorkloadAllocation> findAllocationsByPlanIds(Long tenantId, List<Long> workItemPlanIds) {
        if (workItemPlanIds == null || workItemPlanIds.isEmpty()) {
            return List.of();
        }
        return workItemPlanAllocationRepository.findAllByTenantIdAndWorkItemPlanIdIn(tenantId, workItemPlanIds)
                .stream()
                .map(this::toAllocation)
                .toList();
    }

    private ResourceWorkloadPlan toPlan(WorkItemPlanModel model) {
        var entity = workItemPlanMapper.toEntity(model);
        return new ResourceWorkloadPlan(entity.getId(), entity.getWorkItemId(), entity.getPlannedStart(), entity.getPlannedEnd());
    }

    private ResourceWorkloadItem toItem(WorkItemModel model) {
        var entity = workItemMapper.toEntity(model);
        return new ResourceWorkloadItem(
                entity.getId(),
                entity.getProjectId(),
                entity.getAssigneeId(),
                entity.getTimeOriginalEstimate(),
                entity.getTimeRemainingEstimate()
        );
    }

    private ResourceWorkloadAllocation toAllocation(WorkItemPlanAllocationModel model) {
        var entity = workItemPlanAllocationMapper.toEntity(model);
        return new ResourceWorkloadAllocation(
                entity.getWorkItemPlanId(),
                entity.getWorkItemId(),
                entity.getAssigneeId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getEffortMillis()
        );
    }

    private LocalDateTime toUtcLocalDateTime(Long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }
}
