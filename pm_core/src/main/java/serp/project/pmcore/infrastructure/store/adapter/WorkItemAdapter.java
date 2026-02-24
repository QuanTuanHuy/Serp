/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkItemEntity;
import serp.project.pmcore.core.port.store.IWorkItemPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkItemAdapter implements IWorkItemPort {

    private final IWorkItemRepository workItemRepository;
    private final WorkItemMapper workItemMapper;

    @Override
    public WorkItemEntity saveWorkItem(WorkItemEntity workItem) {
        return workItemMapper.toEntity(
                workItemRepository.save(workItemMapper.toModel(workItem))
        );
    }

    @Override
    public Optional<WorkItemEntity> getWorkItemById(Long id, Long tenantId) {
        return workItemRepository.findByIdAndTenantId(id, tenantId)
                .map(workItemMapper::toEntity);
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByProjectId(Long projectId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndProjectId(tenantId, projectId)
        );
    }

    @Override
    public List<WorkItemEntity> getWorkItemsByIssueTypeId(Long issueTypeId, Long tenantId) {
        return workItemMapper.toEntities(
                workItemRepository.findAllByTenantIdAndIssueTypeId(tenantId, issueTypeId)
        );
    }

    @Override
    public void deleteWorkItemById(Long id, Long tenantId) {
        workItemRepository.deleteByIdAndTenantId(id, tenantId);
    }
}
