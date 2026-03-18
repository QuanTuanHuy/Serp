/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;

import java.util.List;

public interface IWorkflowSchemeItemPort {
    List<WorkflowSchemeItemEntity> createWorkflowSchemeItems(List<WorkflowSchemeItemEntity> items);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    void deleteWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);
}
