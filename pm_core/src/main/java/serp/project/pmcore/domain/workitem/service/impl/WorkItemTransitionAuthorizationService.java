/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemTransitionAuthorizationService;

@Service
@RequiredArgsConstructor
public class WorkItemTransitionAuthorizationService implements IWorkItemTransitionAuthorizationService {

    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;

    public void checkTransitionPermissions(ProjectEntity project,
                                           ProjectPermissionEvaluationContext actorContext) {
        workItemAuthorizationSupportService.checkRequiredPermissions(
                ProjectPermissionSubject.from(project),
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.TRANSITION_ISSUES
        );
    }

    public void checkFieldLevelPermissions(ProjectEntity project,
                                           ProjectPermissionEvaluationContext actorContext,
                                           TransitionWorkItemStatusData data) {
        Long dueDate = WorkItemFieldValueUtils.asNullableLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE));
        workItemAuthorizationSupportService.checkScheduleIssuesPermissionIfNeeded(ProjectPermissionSubject.from(project), actorContext, dueDate);

        if (data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            workItemAuthorizationSupportService.checkSetIssueSecurityPermissionIfNeeded(
                    ProjectPermissionSubject.from(project),
                    actorContext,
                    WorkItemFieldValueUtils.asNullableLong(data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID))
            );
        }
    }

    public Long resolveAssigneeId(ProjectEntity project,
                                  Long currentAssigneeId,
                                  ProjectPermissionEvaluationContext actorContext,
                                  TransitionWorkItemStatusData data) {
        if (!data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            return currentAssigneeId;
        }
        return workItemAuthorizationSupportService.resolveAssigneeId(
                ProjectPermissionSubject.from(project),
                WorkItemFieldValueUtils.asNullableLong(data.getSystemField(WorkItemFieldConstants.ASSIGNEE_ID)),
                actorContext
        );
    }

    public Long resolveSecurityLevelId(Long currentSecurityLevelId,
                                       Long issueSecuritySchemeId,
                                       TransitionWorkItemStatusData data,
                                       Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            return currentSecurityLevelId;
        }

        Long requestedSecurityLevelId = WorkItemFieldValueUtils.asNullableLong(
                data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID));
        if (requestedSecurityLevelId == null) {
            return null;
        }

        if (issueSecuritySchemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Issue security scheme binding is required"
            );
        }

        return issueSecurityService.validateSecurityLevelId(
                issueSecuritySchemeId,
                requestedSecurityLevelId,
                tenantId
        );
    }

    public void checkIssueSecurityAccessIfNeeded(ProjectEntity project,
                                                 WorkItemEntity workItem,
                                                 ProjectPermissionEvaluationContext actorContext,
                                                 Long tenantId) {
        if (workItem.getSecurityLevelId() == null) {
            return;
        }

        issueSecurityService.checkSecurityAccessIfNeeded(project, workItem, actorContext, tenantId);
    }
}
