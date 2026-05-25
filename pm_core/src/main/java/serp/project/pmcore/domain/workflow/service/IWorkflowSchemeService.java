/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import java.util.List;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;

public interface IWorkflowSchemeService {
    WorkflowSchemeEntity createWorkflowScheme(WorkflowSchemeEntity scheme, Long tenantId, Long userId);

    WorkflowSchemeEntity getWorkflowSchemeById(Long workflowSchemeId, Long tenantId);

    WorkflowSchemeEntity getVisibleWorkflowSchemeById(Long workflowSchemeId, Long tenantId);

    WorkflowSchemeEntity getVisibleWorkflowSchemeDetailById(Long workflowSchemeId, Long tenantId);

    PageResult<WorkflowSchemeEntity> listVisibleWorkflowSchemes(Long tenantId, WorkflowSchemeListCriteria criteria);

    WorkflowSchemeEntity updateWorkflowScheme(Long workflowSchemeId, WorkflowSchemeUpdateData data, Long tenantId, Long userId);

    WorkflowSchemeEntity deleteWorkflowScheme(Long workflowSchemeId, Long tenantId, Long userId);

    WorkflowSchemeEntity replaceWorkflowSchemeItems(Long workflowSchemeId,
                                                    List<WorkflowSchemeItemReplacement> items,
                                                    Long tenantId,
                                                    Long userId);

    Long resolveWorkflowId(Long workflowSchemeId, Long issueTypeId, Long tenantId);

    record WorkflowSchemeItemReplacement(Long issueTypeId, Long workflowId) {
    }
}
