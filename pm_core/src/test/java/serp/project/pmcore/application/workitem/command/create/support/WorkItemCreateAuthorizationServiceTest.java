/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
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
class WorkItemCreateAuthorizationServiceTest {

    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private WorkItemCreateAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new WorkItemCreateAuthorizationService(projectPermissionEvaluationService);
    }

    @Test
    void buildActorContextShouldPopulateUserAndGroups() {
        ProjectPermissionEvaluationContext context = service.buildActorContext(99L, Set.of("dev", "qa"));

        assertEquals(99L, context.getUserId());
        assertEquals(Set.of("dev", "qa"), context.getGroupKeys());
    }

    @Test
    void checkCreatePermissionsShouldRequireBrowseAndCreate() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkCreatePermissions(project, actorContext);

        verify(projectPermissionEvaluationService).checkPermission(project, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        verify(projectPermissionEvaluationService).checkPermission(project, actorContext, ProjectPermissionKeys.CREATE_ISSUES);
    }

    @Test
    void resolveAssigneeIdShouldReturnNullWhenAssigneeOmitted() {
        assertNull(service.resolveAssigneeId(ProjectEntity.builder().id(10L).build(), null, service.buildActorContext(1L, Set.of())));
    }

    @Test
    void resolveAssigneeIdShouldRejectNonAssignableUser() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());
        when(projectPermissionEvaluationService.hasPermission(eq(project), any(ProjectPermissionEvaluationContext.class), eq(ProjectPermissionKeys.ASSIGNABLE_USER)))
                .thenReturn(false);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.resolveAssigneeId(project, 100L, actorContext)
        );

        assertEquals(DomainErrorCode.PROJECT_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void checkScheduleIssuesPermissionIfNeededShouldDelegateWhenDueDatePresent() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkScheduleIssuesPermissionIfNeeded(project, actorContext, 1_700_000_000_000L);

        verify(projectPermissionEvaluationService).checkPermission(project, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
    }

    @Test
    void checkSetIssueSecurityPermissionIfNeededShouldDelegateWhenExplicitSecurityProvided() {
        ProjectEntity project = ProjectEntity.builder().id(10L).build();
        ProjectPermissionEvaluationContext actorContext = service.buildActorContext(99L, Set.of());

        service.checkSetIssueSecurityPermissionIfNeeded(project, actorContext, 12L);

        verify(projectPermissionEvaluationService).checkPermission(project, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);
    }
}
