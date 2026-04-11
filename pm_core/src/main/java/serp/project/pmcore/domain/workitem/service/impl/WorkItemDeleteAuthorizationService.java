/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemDeleteAuthorizationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemDeleteAuthorizationService implements IWorkItemDeleteAuthorizationService {

    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;

    @Override
    public void checkDeletePermission(ProjectEntity project,
                                      ProjectPermissionEvaluationContext actorContext) {
        workItemAuthorizationSupportService.checkRequiredPermissions(
                ProjectPermissionSubject.from(project),
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.DELETE_ISSUES
        );
    }

    @Override
    public void checkDeleteSecurityAccess(ProjectEntity project,
                                          WorkItemEntity workItem,
                                          ProjectPermissionEvaluationContext actorContext) {
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
    }

}
