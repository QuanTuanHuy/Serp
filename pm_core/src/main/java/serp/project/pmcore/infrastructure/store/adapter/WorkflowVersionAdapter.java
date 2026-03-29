/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowVersionMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowVersionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowVersionAdapter implements IWorkflowVersionPort {

    private final IWorkflowVersionRepository workflowVersionRepository;
    private final WorkflowVersionMapper workflowVersionMapper;

    @Override
    public WorkflowVersionEntity createWorkflowVersion(WorkflowVersionEntity workflowVersion) {
        return workflowVersionMapper.toEntity(
                workflowVersionRepository.save(workflowVersionMapper.toModel(workflowVersion))
        );
    }

    @Override
    public void updateWorkflowVersion(WorkflowVersionEntity workflowVersion) {
        workflowVersionRepository.save(workflowVersionMapper.toModel(workflowVersion));
    }

    @Override
    public List<WorkflowVersionEntity> createWorkflowVersions(List<WorkflowVersionEntity> workflowVersions) {
        if (workflowVersions == null || workflowVersions.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowVersionMapper.toEntities(
                workflowVersionRepository.saveAll(workflowVersionMapper.toModels(workflowVersions))
        );
    }

    @Override
    public Optional<WorkflowVersionEntity> getWorkflowVersionById(Long id, Long tenantId) {
        return workflowVersionRepository.findByIdAndTenantId(id, tenantId)
                .map(workflowVersionMapper::toEntity);
    }

    @Override
    public Optional<WorkflowVersionEntity> getWorkflowVersionByIdIncludingSystem(Long id, Long tenantId) {
        return workflowVersionRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(workflowVersionMapper::toEntity);
    }

    @Override
    public List<WorkflowVersionEntity> getWorkflowVersionsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId) {
        return workflowVersionMapper.toEntities(
                workflowVersionRepository.findByWorkflowIdAndTenantIdOrSystemTenant(workflowId, tenantId)
        );
    }
}
