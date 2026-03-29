/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;

public interface IWorkflowSchemeItemPort {
    List<WorkflowSchemeItemEntity> createWorkflowSchemeItems(List<WorkflowSchemeItemEntity> items);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    void deleteWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);
}
