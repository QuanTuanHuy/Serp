/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;

public interface IWorkflowSchemePort {
    WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeById(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeByIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeEntity> getWorkflowSchemeWithItems(Long schemeId, Long tenantId);

    List<WorkflowSchemeEntity> listWorkflowSchemes(Long tenantId);

    PageResult<WorkflowSchemeEntity> listWorkflowSchemesIncludingSystem(Long tenantId, WorkflowSchemeListCriteria criteria);

    void updateWorkflowScheme(WorkflowSchemeEntity scheme);

    void deleteWorkflowScheme(Long schemeId, Long tenantId);

    boolean existsByName(Long tenantId, String name);
}
