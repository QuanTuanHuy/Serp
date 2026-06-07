/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanAllocationPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanAllocationMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanAllocationRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkItemPlanAllocationAdapter implements IWorkItemPlanAllocationPort {
    private final IWorkItemPlanAllocationRepository workItemPlanAllocationRepository;
    private final WorkItemPlanAllocationMapper workItemPlanAllocationMapper;

    @Override
    public List<WorkItemPlanAllocationEntity> listByPlanIds(Long tenantId, List<Long> workItemPlanIds) {
        if (workItemPlanIds == null || workItemPlanIds.isEmpty()) {
            return List.of();
        }
        return workItemPlanAllocationMapper.toEntities(
                workItemPlanAllocationRepository.findAllByTenantIdAndWorkItemPlanIdIn(tenantId, workItemPlanIds)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WorkItemPlanAllocationEntity> replaceForPlan(Long tenantId,
                                                             Long workItemPlanId,
                                                             List<WorkItemPlanAllocationEntity> allocations) {
        workItemPlanAllocationRepository.deleteByTenantIdAndWorkItemPlanId(tenantId, workItemPlanId);
        if (allocations == null || allocations.isEmpty()) {
            return List.of();
        }
        return workItemPlanAllocationMapper.toEntities(
                workItemPlanAllocationRepository.saveAll(workItemPlanAllocationMapper.toModels(allocations))
        );
    }
}
