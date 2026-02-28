/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IWorkflowStepPort;
import serp.project.pmcore.core.service.IWorkflowService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService implements IWorkflowService {

    private final IWorkflowStepPort workflowStepPort;

    @Override
    public WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId) {
        return workflowStepPort.getInitialStep(workflowId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.WORKFLOW_STEP_NOT_FOUND));
    }

}
