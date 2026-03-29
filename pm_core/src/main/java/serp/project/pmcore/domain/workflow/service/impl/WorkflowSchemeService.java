/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;
import serp.project.pmcore.domain.shared.exception.AppException;
import serp.project.pmcore.domain.shared.exception.ErrorCode;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowSchemeService implements IWorkflowSchemeService {

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;

    @Override
    public Long resolveWorkflowId(Long workflowSchemeId, Long issueTypeId, Long tenantId) {
        WorkflowSchemeEntity scheme = workflowSchemePort
                .getWorkflowSchemeById(workflowSchemeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEME_NOT_FOUND));

        Long workflowId = workflowSchemeItemPort
                .getWorkflowSchemeItemsBySchemeId(workflowSchemeId, tenantId)
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .findFirst()
                .orElse(scheme.getDefaultWorkflowId());
        if (workflowId == null) {
            throw new AppException(ErrorCode.WORKFLOW_NOT_FOUND);
        }
        return workflowId;
    }

}
