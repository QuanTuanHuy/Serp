/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowStepMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowStepRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowStepAdapter implements IWorkflowStepPort {

    private final IWorkflowStepRepository workflowStepRepository;
    private final WorkflowStepMapper workflowStepMapper;

    @Override
    public List<WorkflowStepEntity> createWorkflowSteps(List<WorkflowStepEntity> steps) {
        if (steps == null || steps.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowStepMapper.toEntities(
                workflowStepRepository.saveAll(workflowStepMapper.toModels(steps))
        );
    }

    @Override
    public List<WorkflowStepEntity> getWorkflowStepsByWorkflowVersionId(Long workflowVersionId, Long tenantId) {
        return workflowStepMapper.toEntities(
                workflowStepRepository.findByWorkflowVersionIdAndTenantId(workflowVersionId, tenantId)
        );
    }

    @Override
    public List<WorkflowStepEntity> getWorkflowStepsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId) {
        return workflowStepMapper.toEntities(
                workflowStepRepository.findByWorkflowVersionIdAndTenantIdOrSystemTenant(workflowVersionId, tenantId)
        );
    }

    @Override
    public Optional<WorkflowStepEntity> getInitialStepByWorkflowVersionId(Long workflowVersionId, Long tenantId) {
        return workflowStepRepository.findInitialStepByWorkflowVersionId(workflowVersionId, tenantId)
                .map(workflowStepMapper::toEntity);
    }

    @Override
    public Optional<WorkflowStepEntity> getWorkflowStepById(Long id, Long tenantId) {
        return workflowStepRepository.findByIdAndTenantId(id, tenantId)
                .map(workflowStepMapper::toEntity);
    }

    @Override
    public boolean existsByStatusIdIncludingSystem(Long statusId, Long tenantId) {
        return workflowStepRepository.existsByStatusIdAndTenantIdOrSystemTenant(statusId, tenantId);
    }
}
