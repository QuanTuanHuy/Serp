/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionEvaluationServiceTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long SCHEME_ID = 820L;
    private static final Long LEAD_USER_ID = 100L;

    @Mock
    private IPermissionSchemeEntryPort permissionSchemeEntryPort;

    @Mock
    private IProjectRoleService projectRoleService;

    @Mock
    private IProjectRoleActorService projectRoleActorService;

    private ProjectPermissionEvaluationService service;

    @BeforeEach
    void setUp() {
        service = new ProjectPermissionEvaluationService(
                permissionSchemeEntryPort,
                projectRoleService,
                projectRoleActorService
        );
    }

    @Test
    void hasPermissionShouldGrantUserEntry() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(42L, Set.of(), null, null);

        mockEntries(List.of(entry(1L, ProjectPermissionKeys.BROWSE_PROJECTS, "USER", "42")));

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldGrantGroupEntryIgnoringCaseAndWhitespace() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(42L, Set.of(" Dev-Team "), null, null);

        mockEntries(List.of(entry(2L, ProjectPermissionKeys.BROWSE_PROJECTS, "GROUP", "dev-team")));

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldGrantReporterAndAssigneeContextualEntries() {
        ProjectEntity project = projectWithScheme();

        ProjectPermissionEvaluationContext reporterContext = context(55L, Set.of(), 55L, null);
        mockEntries(List.of(entry(3L, ProjectPermissionKeys.BROWSE_PROJECTS, "REPORTER", null)));
        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), reporterContext, ProjectPermissionKeys.BROWSE_PROJECTS));

        ProjectPermissionEvaluationContext assigneeContext = context(66L, Set.of(), null, 66L);
        mockEntries(List.of(entry(4L, ProjectPermissionKeys.BROWSE_PROJECTS, "ASSIGNEE", null)));
        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), assigneeContext, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldGrantProjectRoleWhenAnyMatchingRoleHasAssignment() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(77L, Set.of(), null, null);

        mockEntries(List.of(entry(5L, ProjectPermissionKeys.ADMINISTER_PROJECTS, "PROJECT_ROLE", "Administrators")));

        ProjectRoleEntity systemRole = ProjectRoleEntity.builder().id(300L).tenantId(0L).name("Administrators").build();
        ProjectRoleEntity tenantRole = ProjectRoleEntity.builder().id(301L).tenantId(TENANT_ID).name("Administrators").build();

        when(projectRoleService.getProjectRolesByNameIncludingSystem("Administrators", TENANT_ID))
                .thenReturn(List.of(systemRole, tenantRole));

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                300L,
                ProjectRoleActorSubjectType.USER.name(),
                "77"
        )).thenReturn(false);

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                301L,
                ProjectRoleActorSubjectType.USER.name(),
                "77"
        )).thenReturn(true);

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.ADMINISTER_PROJECTS));
    }

    @Test
    void hasPermissionShouldFallbackToProjectLeadWhenSchemeMissing() {
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .leadUserId(LEAD_USER_ID)
                .permissionSchemeId(null)
                .build();

        ProjectPermissionEvaluationContext leadContext = context(LEAD_USER_ID, Set.of(), null, null);
        ProjectPermissionEvaluationContext nonLeadContext = context(999L, Set.of(), null, null);

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), leadContext, ProjectPermissionKeys.ADMINISTER_PROJECTS));
        assertFalse(service.hasPermission(ProjectPermissionSubject.from(project), nonLeadContext, ProjectPermissionKeys.ADMINISTER_PROJECTS));
        assertFalse(service.hasPermission(ProjectPermissionSubject.from(project), leadContext, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldHandleLoggedInUserAlias() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(123L, Set.of(), null, null);

        mockEntries(List.of(entry(6L, ProjectPermissionKeys.BROWSE_PROJECTS, "LOGGED_IN_USER", null)));

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldReturnFalseForUnsupportedContextualGranteeTypes() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(42L, Set.of("dev"), null, null);

        mockEntries(List.of(
                entry(7L, ProjectPermissionKeys.BROWSE_PROJECTS, "APPLICATION_ACCESS", "serp-account"),
                entry(8L, ProjectPermissionKeys.BROWSE_PROJECTS, "USER_CUSTOM_FIELD_VALUE", null),
                entry(9L, ProjectPermissionKeys.BROWSE_PROJECTS, "GROUP_CUSTOM_FIELD_VALUE", null),
                entry(10L, ProjectPermissionKeys.BROWSE_PROJECTS, "ANYONE_ON_WEB", null)
        ));

        assertFalse(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void hasPermissionShouldIgnoreUnknownGranteeTypeWithoutThrowing() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(42L, Set.of("dev"), null, null);

        mockEntries(List.of(entry(11L, ProjectPermissionKeys.BROWSE_PROJECTS, "UNKNOWN_TYPE", null)));

        assertFalse(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));
    }

    @Test
    void checkPermissionShouldThrowAccessDeniedWhenPermissionMissing() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(42L, Set.of(), null, null);
        mockEntries(List.of());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> service.checkPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS)
        );

        assertEquals(DomainErrorCode.PROJECT_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void hasPermissionShouldEvaluateProjectRoleByGroupAssignment() {
        ProjectEntity project = projectWithScheme();
        ProjectPermissionEvaluationContext context = context(77L, Set.of(" Team-Alpha "), null, null);

        mockEntries(List.of(entry(12L, ProjectPermissionKeys.BROWSE_PROJECTS, "PROJECT_ROLE", "Users")));

        ProjectRoleEntity usersRole = ProjectRoleEntity.builder().id(501L).tenantId(TENANT_ID).name("Users").build();
        when(projectRoleService.getProjectRolesByNameIncludingSystem("Users", TENANT_ID))
                .thenReturn(List.of(usersRole));

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                501L,
                ProjectRoleActorSubjectType.USER.name(),
                "77"
        )).thenReturn(false);

        when(projectRoleActorService.hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                501L,
                ProjectRoleActorSubjectType.GROUP.name(),
                "team-alpha"
        )).thenReturn(true);

        assertTrue(service.hasPermission(ProjectPermissionSubject.from(project), context, ProjectPermissionKeys.BROWSE_PROJECTS));

        verify(projectRoleActorService).hasRoleAssignment(
                TENANT_ID,
                PROJECT_ID,
                501L,
                ProjectRoleActorSubjectType.GROUP.name(),
                "team-alpha"
        );
    }

    private ProjectEntity projectWithScheme() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .leadUserId(LEAD_USER_ID)
                .permissionSchemeId(SCHEME_ID)
                .build();
    }

    private ProjectPermissionEvaluationContext context(Long userId,
                                                       Set<String> groupKeys,
                                                       Long reporterUserId,
                                                       Long assigneeUserId) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .reporterUserId(reporterUserId)
                .assigneeUserId(assigneeUserId)
                .build();
    }

    private PermissionSchemeEntryEntity entry(Long id,
                                              String permissionKey,
                                              String granteeType,
                                              String granteeRef) {
        return PermissionSchemeEntryEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .schemeId(SCHEME_ID)
                .permissionKey(permissionKey)
                .granteeType(granteeType)
                .granteeRef(granteeRef)
                .build();
    }

    private void mockEntries(List<PermissionSchemeEntryEntity> entries) {
        when(permissionSchemeEntryPort.getPermissionSchemeEntriesBySchemeId(SCHEME_ID, TENANT_ID))
                .thenReturn(entries);
    }
}
