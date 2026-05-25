/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemAuthorizationSupportServiceTest {

    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private WorkItemAuthorizationSupportService service;

    @BeforeEach
    void setUp() {
        service = new WorkItemAuthorizationSupportService(projectPermissionEvaluationService);
    }

    @Test
    void buildActorContextShouldPopulateUserGroupsReporterAndAssignee() {
        ProjectPermissionEvaluationContext context = service.buildActorContext(99L, Set.of("dev", "qa"), 11L, 12L);

        assertEquals(99L, context.getUserId());
        assertEquals(Set.of("dev", "qa"), context.getGroupKeys());
        assertEquals(11L, context.getReporterUserId());
        assertEquals(12L, context.getAssigneeUserId());
    }

    @Test
    void checkRequiredPermissionsShouldDelegateAllPermissions() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkRequiredPermissions(subject, actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.CREATE_ISSUES);

        verify(projectPermissionEvaluationService).checkPermission(subject, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        verify(projectPermissionEvaluationService).checkPermission(subject, actorContext, ProjectPermissionKeys.CREATE_ISSUES);
    }

    @Test
    void resolveAssigneeIdShouldReturnNullWhenAssigneeOmitted() {
        assertNull(service.resolveAssigneeId(ProjectPermissionSubject.from(ProjectEntity.builder().id(10L).build()), null, service.buildActorContext(1L, Set.of())));
    }

    @Test
    void resolveAssigneeIdShouldRejectNonAssignableUser() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());
        when(projectPermissionEvaluationService.hasPermission(eq(subject), any(ProjectPermissionEvaluationContext.class), eq(ProjectPermissionKeys.ASSIGNABLE_USER)))
                .thenReturn(false);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.resolveAssigneeId(subject, 100L, actorContext)
        );

        assertEquals(DomainErrorCode.PROJECT_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void checkScheduleIssuesPermissionShouldDelegate() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkScheduleIssuesPermission(subject, actorContext);

        verify(projectPermissionEvaluationService).checkPermission(subject, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
    }

    @Test
    void checkSetIssueSecurityPermissionIfNeededShouldDelegateWhenExplicitSecurityProvided() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionSubject subject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkSetIssueSecurityPermissionIfNeeded(subject, actorContext, 12L);

        verify(projectPermissionEvaluationService).checkPermission(subject, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);
    }
}
