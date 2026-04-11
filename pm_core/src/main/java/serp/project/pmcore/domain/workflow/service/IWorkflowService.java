/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public interface IWorkflowService {

    WorkflowEntity resolveWorkflow(Long workflowSchemeId,
                                   Long issueTypeId,
                                   Long tenantId);

    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
