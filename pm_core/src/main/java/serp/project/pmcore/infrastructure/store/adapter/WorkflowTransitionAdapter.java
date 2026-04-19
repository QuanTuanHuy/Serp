/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowTransitionMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowTransitionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowTransitionAdapter implements IWorkflowTransitionPort {

    private final IWorkflowTransitionRepository workflowTransitionRepository;
    private final WorkflowTransitionMapper workflowTransitionMapper;

    @Override
    public List<WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowTransitionEntity> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowTransitionMapper.toEntities(
                workflowTransitionRepository.saveAll(workflowTransitionMapper.toModels(transitions))
        );
    }

    @Override
    public List<WorkflowTransitionEntity> updateWorkflowTransitions(List<WorkflowTransitionEntity> transitions) {
        if (transitions == null || transitions.isEmpty()) {
            return new ArrayList<>();
        }
        return workflowTransitionMapper.toEntities(
                workflowTransitionRepository.saveAll(workflowTransitionMapper.toModels(transitions))
        );
    }

    @Override
    public List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionId(Long workflowVersionId, Long tenantId) {
        return workflowTransitionMapper.toEntities(
                workflowTransitionRepository.findByWorkflowVersionIdAndTenantId(workflowVersionId, tenantId)
        );
    }

    @Override
    public List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId) {
        return workflowTransitionMapper.toEntities(
                workflowTransitionRepository.findByWorkflowVersionIdAndTenantIdOrSystemTenant(workflowVersionId, tenantId)
        );
    }

    @Override
    public Optional<WorkflowTransitionEntity> getWorkflowTransitionByIdAndWorkflowVersionId(Long id, Long workflowVersionId, Long tenantId) {
        return workflowTransitionRepository.findById(id)
                .filter(workflowTransition ->
                        workflowTransition.getWorkflowVersionId().equals(workflowVersionId) &&
                        workflowTransition.getTenantId().equals(tenantId))
                .map(workflowTransitionMapper::toEntity);

    }
}
