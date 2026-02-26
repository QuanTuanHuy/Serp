/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkflowSchemeEntity;

import java.util.Optional;

public interface IWorkflowSchemePort {
    WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
