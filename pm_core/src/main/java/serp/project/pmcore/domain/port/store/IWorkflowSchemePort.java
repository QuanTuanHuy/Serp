/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;

import java.util.Optional;

public interface IWorkflowSchemePort {
    WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
