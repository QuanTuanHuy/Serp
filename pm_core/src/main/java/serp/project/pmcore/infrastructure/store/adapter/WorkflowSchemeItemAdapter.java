/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<WorkflowSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId) {
        return workflowSchemeItemRepository.findFirstByTenantIdAndSchemeIdAndIssueTypeId(tenantId, schemeId, issueTypeId)
                .map(workflowSchemeItemMapper::toEntity);
    }

    @Override
    public void deleteWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        workflowSchemeItemRepository.deleteBySchemeIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsByIssueTypeId(Long issueTypeId, Long tenantId) {
        return workflowSchemeItemRepository.existsByIssueTypeIdAndTenantId(issueTypeId, tenantId);
    }
}
