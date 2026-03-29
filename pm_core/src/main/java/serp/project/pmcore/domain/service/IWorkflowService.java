/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public interface IWorkflowService {
    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
