/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.WorkflowEntity;
import serp.project.pmcore.core.port.store.IWorkflowPort;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkflowAdapter implements IWorkflowPort {

    private final IWorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    @Override
    public Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId) {
        return workflowRepository.findByIdAndTenantId(id, tenantId)
                .map(workflowMapper::toEntity);
    }

    @Override
    public Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId) {
        return workflowRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(workflowMapper::toEntity);
    }
}
