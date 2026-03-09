/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkflowEntity;

import java.util.Optional;

public interface IWorkflowPort {
    Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId);

    Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId);
}
