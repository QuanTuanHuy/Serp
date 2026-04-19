/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.service.IWorklogAuthorizationService;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WorklogAuthorizationService implements IWorklogAuthorizationService {

    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IIssueSecurityService issueSecurityService;

    @Override
    public void checkReadAccess(ProjectEntity project,
                                WorkItemEntity workItem,
                                ProjectPermissionEvaluationContext actorContext) {
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        workItemAuthorizationSupportService.checkRequiredPermissions(
                subject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
    }

    @Override
    public void checkCreateAccess(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  ProjectPermissionEvaluationContext actorContext) {
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        workItemAuthorizationSupportService.checkRequiredPermissions(
                subject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.WORK_ON_ISSUES
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
    }

    @Override
    public void checkUpdateAccess(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  WorklogEntity worklog,
                                  ProjectPermissionEvaluationContext actorContext) {
        checkReadAccess(project, workItem, actorContext);
        checkOwnOrAllPermission(
                project,
                actorContext,
                worklog.getAuthorId(),
                ProjectPermissionKeys.EDIT_ALL_WORKLOGS,
                ProjectPermissionKeys.EDIT_OWN_WORKLOGS
        );
    }

    @Override
    public void checkDeleteAccess(ProjectEntity project,
                                  WorkItemEntity workItem,
                                  WorklogEntity worklog,
                                  ProjectPermissionEvaluationContext actorContext) {
        checkReadAccess(project, workItem, actorContext);
        checkOwnOrAllPermission(
                project,
                actorContext,
                worklog.getAuthorId(),
                ProjectPermissionKeys.DELETE_ALL_WORKLOGS,
                ProjectPermissionKeys.DELETE_OWN_WORKLOGS
        );
    }

    private void checkOwnOrAllPermission(ProjectEntity project,
                                         ProjectPermissionEvaluationContext actorContext,
                                         Long authorId,
                                         String allPermissionKey,
                                         String ownPermissionKey) {
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        if (projectPermissionEvaluationService.hasPermission(subject, actorContext, allPermissionKey)) {
            return;
        }

        boolean hasOwnPermission = projectPermissionEvaluationService.hasPermission(subject, actorContext, ownPermissionKey);
        if (hasOwnPermission) {
            if (Objects.equals(actorContext.getUserId(), authorId)) {
                return;
            }
            throw new BusinessRuleViolationException(DomainErrorCode.WORKLOG_NOT_OWNER);
        }

        throw AccessDeniedException.projectPermission(allPermissionKey + "|" + ownPermissionKey, project.getId());
    }
}
