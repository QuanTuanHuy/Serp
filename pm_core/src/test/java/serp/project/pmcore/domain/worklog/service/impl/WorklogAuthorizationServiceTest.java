/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorklogAuthorizationServiceTest {

    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;

    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Mock
    private IIssueSecurityService issueSecurityService;

    private WorklogAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new WorklogAuthorizationService(
                workItemAuthorizationSupportService,
                projectPermissionEvaluationService,
                issueSecurityService
        );
    }

    @Test
    void checkUpdateAccessShouldRejectNonOwnerWhenOnlyOwnPermissionGranted() {
        ProjectEntity project = ProjectEntity.builder().id(10L).tenantId(1L).build();
        WorkItemEntity workItem = WorkItemEntity.builder().id(20L).projectId(10L).tenantId(1L).build();
        WorklogEntity worklog = WorklogEntity.builder().id(30L).workItemId(20L).authorId(999L).build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(100L)
                .groupKeys(Set.of("dev"))
                .build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);

        when(projectPermissionEvaluationService.hasPermission(subject, actorContext, ProjectPermissionKeys.EDIT_ALL_WORKLOGS))
                .thenReturn(false);
        when(projectPermissionEvaluationService.hasPermission(subject, actorContext, ProjectPermissionKeys.EDIT_OWN_WORKLOGS))
                .thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.checkUpdateAccess(project, workItem, worklog, actorContext)
        );

        assertEquals(DomainErrorCode.WORKLOG_NOT_OWNER, exception.getErrorCode());
        verify(workItemAuthorizationSupportService).checkRequiredPermissions(
                eq(subject),
                eq(actorContext),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        verify(issueSecurityService).checkSecurityAccessIfNeeded(any(), eq(actorContext));
    }

    @Test
    void checkDeleteAccessShouldRejectWhenNoDeletePermissionGranted() {
        ProjectEntity project = ProjectEntity.builder().id(10L).tenantId(1L).build();
        WorkItemEntity workItem = WorkItemEntity.builder().id(20L).projectId(10L).tenantId(1L).build();
        WorklogEntity worklog = WorklogEntity.builder().id(30L).workItemId(20L).authorId(100L).build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(100L)
                .groupKeys(Set.of())
                .build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);

        when(projectPermissionEvaluationService.hasPermission(subject, actorContext, ProjectPermissionKeys.DELETE_ALL_WORKLOGS))
                .thenReturn(false);
        when(projectPermissionEvaluationService.hasPermission(subject, actorContext, ProjectPermissionKeys.DELETE_OWN_WORKLOGS))
                .thenReturn(false);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.checkDeleteAccess(project, workItem, worklog, actorContext)
        );

        assertEquals(DomainErrorCode.PROJECT_PERMISSION_DENIED, exception.getErrorCode());
    }
}
