/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemDeleteAuthorizationServiceTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long USER_ID = 99L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long SCHEME_ID = 700L;
    private static final Long SECURITY_LEVEL_ID = 701L;

    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Mock
    private IIssueSecurityLevelPort issueSecurityLevelPort;

    @Mock
    private IIssueSecurityLevelMemberPort issueSecurityLevelMemberPort;

    @Mock
    private IProjectRoleService projectRoleService;

    @Mock
    private IProjectRoleActorService projectRoleActorService;

    @InjectMocks
    private WorkItemDeleteAuthorizationService service;

    @Test
    void checkDeletePermissionShouldRequireBrowseAndDelete() {
        ProjectEntity project = project();
        ProjectPermissionEvaluationContext actorContext = actorContext(USER_ID, Set.of("dev"));

        service.checkDeletePermission(project, actorContext);

        verify(projectPermissionEvaluationService).checkPermission(
                project,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        verify(projectPermissionEvaluationService).checkPermission(
                project,
                actorContext,
                ProjectPermissionKeys.DELETE_ISSUES
        );
    }

    @Test
    void checkDeleteSecurityAccessShouldSkipWhenWorkItemHasNoSecurityLevel() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, null);

        service.checkDeleteSecurityAccess(project, workItem, actorContext(USER_ID, Set.of("dev")));
    }

    @Test
    void checkDeleteSecurityAccessShouldRejectWhenProjectHasNoIssueSecurityScheme() {
        ProjectEntity project = project();
        setField(project, "issueSecuritySchemeId", null);
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.checkDeleteSecurityAccess(project, workItem, actorContext(USER_ID, Set.of("dev")))
        );

        assertEquals(DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME, getField(exception, "errorCode"));
    }

    @Test
    void checkDeleteSecurityAccessShouldRejectWhenSecurityLevelNotInProjectScheme() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.empty());

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.checkDeleteSecurityAccess(project, workItem, actorContext(USER_ID, Set.of("dev")))
        );

        assertEquals(DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME, getField(exception, "errorCode"));
    }

    @Test
    void checkDeleteSecurityAccessShouldRejectWhenNoMemberMatchesActorContext() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        IssueSecurityLevelEntity level = new IssueSecurityLevelEntity();
        setField(level, "id", SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.of(level));

        when(issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(SECURITY_LEVEL_ID, TENANT_ID))
                .thenReturn(List.of(member("GROUP", "ops")));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.checkDeleteSecurityAccess(project, workItem, actorContext(USER_ID, Set.of("dev")))
        );

        assertEquals(DomainErrorCode.SECURITY_LEVEL_ACCESS_DENIED, getField(exception, "errorCode"));
    }

    @Test
    void checkDeleteSecurityAccessShouldAllowWhenGroupMemberMatchesIgnoringCaseAndWhitespace() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        IssueSecurityLevelEntity level = new IssueSecurityLevelEntity();
        setField(level, "id", SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.of(level));

        when(issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(SECURITY_LEVEL_ID, TENANT_ID))
                .thenReturn(List.of(member("GROUP", "dev-team")));

        service.checkDeleteSecurityAccess(
                project,
                workItem,
                actorContext(USER_ID, Set.of(" Dev-Team "))
        );
    }

    @Test
    void checkDeleteSecurityAccessShouldAllowWhenProjectRoleUserAssignmentMatches() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        IssueSecurityLevelEntity level = new IssueSecurityLevelEntity();
        setField(level, "id", SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.of(level));

        when(issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(SECURITY_LEVEL_ID, TENANT_ID))
                .thenReturn(List.of(member("PROJECT_ROLE", "Administrators")));

        ProjectRoleEntity administratorsRole = new ProjectRoleEntity();
        setField(administratorsRole, "id", 501L);
        when(projectRoleService.getProjectRolesByNameIncludingSystem("Administrators", TENANT_ID))
                .thenReturn(List.of(administratorsRole));

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                501L,
                ProjectRoleActorSubjectType.USER.name(),
                String.valueOf(USER_ID)
        )).thenReturn(true);

        service.checkDeleteSecurityAccess(project, workItem, actorContext(USER_ID, Set.of("dev")));

        verify(projectRoleActorService).hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                501L,
                ProjectRoleActorSubjectType.USER.name(),
                String.valueOf(USER_ID)
        );
    }

    @Test
    void checkDeleteSecurityAccessShouldAllowWhenProjectRoleGroupAssignmentMatches() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        IssueSecurityLevelEntity level = new IssueSecurityLevelEntity();
        setField(level, "id", SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.of(level));

        when(issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(SECURITY_LEVEL_ID, TENANT_ID))
                .thenReturn(List.of(member("PROJECT_ROLE", "Users")));

        ProjectRoleEntity usersRole = new ProjectRoleEntity();
        setField(usersRole, "id", 601L);
        when(projectRoleService.getProjectRolesByNameIncludingSystem("Users", TENANT_ID))
                .thenReturn(List.of(usersRole));

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                601L,
                ProjectRoleActorSubjectType.USER.name(),
                String.valueOf(USER_ID)
        )).thenReturn(false);

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                601L,
                ProjectRoleActorSubjectType.GROUP.name(),
                "team-alpha"
        )).thenReturn(true);

        service.checkDeleteSecurityAccess(
                project,
                workItem,
                actorContext(USER_ID, Set.of(" Team-Alpha "))
        );

        verify(projectRoleActorService).hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                601L,
                ProjectRoleActorSubjectType.GROUP.name(),
                "team-alpha"
        );
    }

    @Test
    void checkDeleteSecurityAccessShouldAllowReporterSubjectWhenUserIsReporter() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem(PROJECT_ID, SECURITY_LEVEL_ID);

        IssueSecurityLevelEntity level = new IssueSecurityLevelEntity();
        setField(level, "id", SECURITY_LEVEL_ID);

        when(issueSecurityLevelPort.getIssueSecurityLevelByIdAndSchemeId(
                SECURITY_LEVEL_ID,
                SCHEME_ID,
                TENANT_ID
        )).thenReturn(Optional.of(level));

        when(issueSecurityLevelMemberPort.getIssueSecurityLevelMembersByLevelId(SECURITY_LEVEL_ID, TENANT_ID))
                .thenReturn(List.of(member("REPORTER", null)));

        ProjectPermissionEvaluationContext context = actorContext(USER_ID, Set.of());
        setField(context, "reporterUserId", USER_ID);

        service.checkDeleteSecurityAccess(project, workItem, context);
    }

    private ProjectEntity project() {
        ProjectEntity project = new ProjectEntity();
        setField(project, "id", PROJECT_ID);
        setField(project, "tenantId", TENANT_ID);
        setField(project, "issueSecuritySchemeId", SCHEME_ID);
        setField(project, "leadUserId", 55L);
        return project;
    }

    private WorkItemEntity workItem(Long projectId, Long securityLevelId) {
        WorkItemEntity workItem = new WorkItemEntity();
        setField(workItem, "id", WORK_ITEM_ID);
        setField(workItem, "projectId", projectId);
        setField(workItem, "securityLevelId", securityLevelId);
        return workItem;
    }

    private ProjectPermissionEvaluationContext actorContext(Long userId, Set<String> groups) {
        ProjectPermissionEvaluationContext context = new ProjectPermissionEvaluationContext();
        setField(context, "userId", userId);
        setField(context, "groupKeys", groups);
        return context;
    }

    private IssueSecurityLevelMemberEntity member(String subjectType, String subjectRef) {
        IssueSecurityLevelMemberEntity member = new IssueSecurityLevelMemberEntity();
        setField(member, "subjectType", subjectType);
        setField(member, "subjectRef", subjectRef);
        return member;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = resolveField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }

    private Object getField(Object target, String fieldName) {
        try {
            Field field = resolveField(target.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read field: " + fieldName, e);
        }
    }

    private Field resolveField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field not found: " + fieldName + " on " + type.getName());
    }
}
