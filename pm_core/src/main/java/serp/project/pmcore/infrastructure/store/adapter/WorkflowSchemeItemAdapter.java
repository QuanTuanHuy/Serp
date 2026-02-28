/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.core.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowSchemeItemAdapter implements IWorkflowSchemeItemPort {

    private final IWorkflowSchemeItemRepository workflowSchemeItemRepository;
    private final WorkflowSchemeItemMapper workflowSchemeItemMapper;

    @Override
    public List<WorkflowSchemeItemEntity> createWorkflowSchemeItems(List<WorkflowSchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowSchemeItemMapper.toEntities(
                workflowSchemeItemRepository.saveAll(workflowSchemeItemMapper.toModels(items))
        );
    }

    @Override
    public List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        return workflowSchemeItemMapper.toEntities(
                workflowSchemeItemRepository.findAllByTenantIdAndSchemeId(tenantId, schemeId)
        );
    }

    @Override
    public List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return workflowSchemeItemMapper.toEntities(
                workflowSchemeItemRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public void deleteWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        workflowSchemeItemRepository.deleteBySchemeIdAndTenantId(schemeId, tenantId);
    }
}
