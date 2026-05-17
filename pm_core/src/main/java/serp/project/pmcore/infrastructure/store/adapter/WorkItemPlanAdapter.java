/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkItemPlanAdapter implements IWorkItemPlanPort {
    private final IWorkItemPlanRepository workItemPlanRepository;
    private final WorkItemPlanMapper workItemPlanMapper;

    @Override
    public List<WorkItemPlanEntity> listActivePlansByWorkItemIds(Long tenantId, List<Long> workItemIds) {
        if (workItemIds == null || workItemIds.isEmpty()) {
            return List.of();
        }
        return workItemPlanMapper.toEntities(workItemPlanRepository.findAllByTenantIdAndWorkItemIdIn(tenantId, workItemIds));
    }

    @Override
    public Optional<WorkItemPlanEntity> getActivePlanByWorkItemId(Long tenantId, Long workItemId) {
        return workItemPlanRepository.findByTenantIdAndWorkItemId(tenantId, workItemId)
                .map(workItemPlanMapper::toEntity);
    }

    @Override
    public WorkItemPlanEntity upsertActivePlan(WorkItemPlanEntity plan) {
        Optional<WorkItemPlanModel> existing = workItemPlanRepository.findByTenantIdAndWorkItemId(
                plan.getTenantId(),
                plan.getWorkItemId()
        );
        WorkItemPlanModel model = workItemPlanMapper.toModel(plan);
        existing.ifPresent(current -> {
            model.setId(current.getId());
            model.setCreatedAt(current.getCreatedAt());
            model.setCreatedBy(current.getCreatedBy());
        });
        return workItemPlanMapper.toEntity(workItemPlanRepository.save(model));
    }
}
