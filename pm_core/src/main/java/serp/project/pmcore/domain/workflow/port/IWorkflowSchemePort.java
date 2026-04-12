/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;

public interface IWorkflowSchemePort {
    WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
