/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkAuthorizationService;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;

@Service
@RequiredArgsConstructor
public class IssueLinkAuthorizationService implements IIssueLinkAuthorizationService {

    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;

    @Override
    public void checkReadAccess(ProjectEntity project,
                                WorkItemEntity workItem,
                                ProjectPermissionEvaluationContext actorContext) {
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        workItemAuthorizationSupportService.checkRequiredPermissions(
                subject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.LINK_ISSUES
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
    }

    @Override
    public void checkWriteAccess(ProjectEntity project,
                                 WorkItemEntity workItem,
                                 ProjectPermissionEvaluationContext actorContext) {
        checkReadAccess(project, workItem, actorContext);
    }
}
