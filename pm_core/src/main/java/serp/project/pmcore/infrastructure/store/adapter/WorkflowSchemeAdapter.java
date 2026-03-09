/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.core.port.store.IWorkflowSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowSchemeAdapter implements IWorkflowSchemePort {

    private final IWorkflowSchemeRepository workflowSchemeRepository;
    private final WorkflowSchemeMapper workflowSchemeMapper;

    @Override
    public WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme) {
        return workflowSchemeMapper.toEntity(
                workflowSchemeRepository.save(workflowSchemeMapper.toModel(scheme))
        );
    }

    @Override
    public Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId) {
        return workflowSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(workflowSchemeMapper::toEntity);
    }

    @Override
    public Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return workflowSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(workflowSchemeMapper::toEntity);
    }
}
