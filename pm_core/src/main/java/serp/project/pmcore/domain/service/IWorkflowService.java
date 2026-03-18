/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;

public interface IWorkflowService {
    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
