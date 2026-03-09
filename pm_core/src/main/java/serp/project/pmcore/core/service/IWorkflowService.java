/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;

public interface IWorkflowService {
    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
