/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
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
    public List<WorkflowStepEntity> getWorkflowStepsByWorkflowId(Long workflowId, Long tenantId) {
        return workflowStepMapper.toEntities(
                workflowStepRepository.findByWorkflowIdAndTenantId(workflowId, tenantId)
        );
    }

    @Override
    public List<WorkflowStepEntity> getWorkflowStepsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId) {
        return workflowStepMapper.toEntities(
                workflowStepRepository.findByWorkflowIdAndTenantIdOrSystemTenant(workflowId, tenantId)
        );
    }

    @Override
    public Optional<WorkflowStepEntity> getInitialStep(Long workflowId, Long tenantId) {
        return workflowStepRepository.findInitialStepByWorkflowId(workflowId, tenantId)
                .map(workflowStepMapper::toEntity);
    }
}
