/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

@Component
@RequiredArgsConstructor
public class WorkItemWriteAdapter implements IWorkItemWritePort {

    private final IWorkItemRepository workItemRepository;
    private final WorkItemMapper workItemMapper;

    @Override
    public WorkItemEntity saveWorkItem(WorkItemEntity workItem) {
        return workItemMapper.toEntity(
                workItemRepository.save(workItemMapper.toModel(workItem))
        );
    }

    @Override
    public void deleteWorkItemById(Long id, Long tenantId) {
        workItemRepository.deleteByIdAndTenantId(id, tenantId);
    }
}
