/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.ErrorCode;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService implements IWorkflowService {

    private final IWorkflowPort workflowPort;
    private final IWorkflowStepPort workflowStepPort;

    @Override
    public WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId) {
        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKFLOW_NOT_FOUND));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new AppException(ErrorCode.WORKFLOW_STEP_NOT_FOUND);
        }

        return workflowStepPort.getInitialStepByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKFLOW_STEP_NOT_FOUND));
    }

}
