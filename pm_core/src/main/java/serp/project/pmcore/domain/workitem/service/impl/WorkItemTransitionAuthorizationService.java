/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateAuthorizationService;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemTransitionAuthorizationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemTransitionAuthorizationService implements IWorkItemTransitionAuthorizationService {

    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final WorkItemCreateAuthorizationService workItemCreateAuthorizationService;

    private final IIssueSecurityService issueSecurityService;

    public void checkTransitionPermissions(ProjectEntity project,
                                           ProjectPermissionEvaluationContext actorContext) {
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.TRANSITION_ISSUES);
    }

    public void checkFieldLevelPermissions(ProjectEntity project,
                                           ProjectPermissionEvaluationContext actorContext,
                                           TransitionWorkItemStatusData data) {
        Long dueDate = asNullableLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE));
        workItemCreateAuthorizationService.checkScheduleIssuesPermissionIfNeeded(project, actorContext, dueDate);

        if (data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            workItemCreateAuthorizationService.checkSetIssueSecurityPermissionIfNeeded(
                    project,
                    actorContext,
                    asNullableLong(data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID))
            );
        }
    }

    public Long resolveAssigneeId(ProjectEntity project,
                                  ProjectPermissionEvaluationContext actorContext,
                                  WorkItemEntity workItem,
                                  TransitionWorkItemStatusData data) {
        if (!data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            return workItem.getAssigneeId();
        }
        return workItemCreateAuthorizationService.resolveAssigneeId(
                project,
                asNullableLong(data.getSystemField(WorkItemFieldConstants.ASSIGNEE_ID)),
                actorContext
        );
    }

    public Long resolveSecurityLevelId(ProjectEntity project,
                                       WorkItemEntity workItem,
                                       TransitionWorkItemStatusData data,
                                       Long tenantId) {
        if (!data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            return workItem.getSecurityLevelId();
        }

        Long requestedSecurityLevelId = asNullableLong(data.getSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID));
        if (requestedSecurityLevelId == null) {
            return null;
        }

        if (project.getIssueSecuritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                    "Project has no issue security scheme binding: projectId=" + project.getId()
            );
        }

        boolean inScheme = issueSecurityLevelPort
                .getIssueSecurityLevelByIdAndSchemeId(
                        requestedSecurityLevelId, project.getIssueSecuritySchemeId(), tenantId)
                .isPresent();

        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in project scheme: projectId=" + project.getId()
                            + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
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

    private Long asNullableLong(Object rawValue) {
        switch (rawValue) {
            case null -> {
                return null;
            }
            case Number number -> {
                return number.longValue();
            }
            case String text -> {
                if (text.isBlank()) {
                    return null;
                }
                return Long.valueOf(text.trim());
            }
            default -> {
            }
        }
        throw new IllegalArgumentException("Expected long-compatible value but got: " + rawValue.getClass().getName());
    }
}