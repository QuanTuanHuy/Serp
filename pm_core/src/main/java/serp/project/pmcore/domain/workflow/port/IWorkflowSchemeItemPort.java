/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;

public interface IWorkflowSchemeItemPort {
    List<WorkflowSchemeItemEntity> createWorkflowSchemeItems(List<WorkflowSchemeItemEntity> items);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    List<WorkflowSchemeItemEntity> getWorkflowSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    Optional<WorkflowSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId);

    void deleteWorkflowSchemeItemsBySchemeId(Long schemeId, Long tenantId);
}
