/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.exception.AppException;
import serp.project.pmcore.domain.exception.ErrorCode;
import serp.project.pmcore.domain.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;
import serp.project.pmcore.domain.service.IWorkflowSchemeService;

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
