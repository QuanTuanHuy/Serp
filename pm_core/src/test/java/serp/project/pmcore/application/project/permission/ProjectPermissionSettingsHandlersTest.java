/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.permission.command.PermissionGrantData;
import serp.project.pmcore.application.project.permission.command.ReplaceProjectPermissionGrantsCommand;
import serp.project.pmcore.application.project.permission.command.ReplaceProjectPermissionGrantsCommandHandler;
import serp.project.pmcore.application.project.permission.query.GetProjectPermissionSettingsQuery;
import serp.project.pmcore.application.project.permission.query.GetProjectPermissionSettingsQueryHandler;
import serp.project.pmcore.application.project.permission.query.ProjectPermissionSettingsView;
import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.domain.permission.port.IPermissionDefinitionPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemePort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.enums.ProjectPermissionGranteeType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionSettingsHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long SCHEME_ID = 820L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IPermissionSchemePort permissionSchemePort;
    @Mock
    private IPermissionSchemeEntryPort permissionSchemeEntryPort;
    @Mock
    private IPermissionDefinitionPort permissionDefinitionPort;

    private GetProjectPermissionSettingsQueryHandler queryHandler;
    private ReplaceProjectPermissionGrantsCommandHandler replaceHandler;

    @BeforeEach
    void setUp() {
        queryHandler = new GetProjectPermissionSettingsQueryHandler(
                projectService,
                projectPermissionEvaluationService,
                permissionSchemePort,
                permissionSchemeEntryPort,
                permissionDefinitionPort
        );
        replaceHandler = new ReplaceProjectPermissionGrantsCommandHandler(
                projectService,
                projectReadPort,
                projectPermissionEvaluationService,
                permissionSchemePort,
                permissionSchemeEntryPort,
                permissionDefinitionPort,
                null
        );
    }

    @Test
    void getSettingsShouldReturnDefinitionsAndGrantsForProjectPermissionScheme() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(permissionSchemePort.getPermissionSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(scheme()));
        when(permissionDefinitionPort.getPermissionDefinitionsIncludingSystem(TENANT_ID))
                .thenReturn(List.of(definition(ProjectPermissionKeys.ADMINISTER_PROJECTS), definition(ProjectPermissionKeys.BROWSE_PROJECTS)));
        when(permissionSchemeEntryPort.getPermissionSchemeEntriesBySchemeIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(
                        entry(1L, ProjectPermissionKeys.ADMINISTER_PROJECTS, ProjectPermissionGranteeType.PROJECT_LEAD.name(), null),
                        entry(2L, ProjectPermissionKeys.BROWSE_PROJECTS, ProjectPermissionGranteeType.PROJECT_ROLE.name(), "Developers")
                ));

        ProjectPermissionSettingsView result = queryHandler.handle(new GetProjectPermissionSettingsQuery(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(SCHEME_ID, result.scheme().id());
        assertEquals("Project copy", result.scheme().name());
        assertEquals(2, result.permissions().size());
        assertEquals(2, result.grants().size());
        assertEquals("Developers", result.grants().get(1).granteeRef());
    }

    @Test
    void replaceGrantsShouldValidateAndPersistProjectSchemeEntries() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(permissionSchemePort.getPermissionSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(scheme()));
        when(projectReadPort.countActiveProjectsByPermissionSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(1L);
        when(permissionDefinitionPort.getPermissionDefinitionsIncludingSystem(TENANT_ID))
                .thenReturn(List.of(definition(ProjectPermissionKeys.ADMINISTER_PROJECTS), definition(ProjectPermissionKeys.BROWSE_PROJECTS)));
        when(permissionSchemeEntryPort.createPermissionSchemeEntries(org.mockito.ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectPermissionSettingsView result = replaceHandler.handle(new ReplaceProjectPermissionGrantsCommand(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins"),
                List.of(
                        new PermissionGrantData(ProjectPermissionKeys.ADMINISTER_PROJECTS, ProjectPermissionGranteeType.PROJECT_LEAD.name(), null, null),
                        new PermissionGrantData(ProjectPermissionKeys.BROWSE_PROJECTS, ProjectPermissionGranteeType.PROJECT_ROLE.name(), "Developers", null)
                )
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                org.mockito.ArgumentMatchers.any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(permissionSchemeEntryPort).deletePermissionSchemeEntriesBySchemeId(SCHEME_ID, TENANT_ID, USER_ID);

        ArgumentCaptor<List<PermissionSchemeEntryEntity>> entriesCaptor = ArgumentCaptor.captor();
        verify(permissionSchemeEntryPort).createPermissionSchemeEntries(entriesCaptor.capture());
        assertEquals(2, entriesCaptor.getValue().size());
        assertEquals(SCHEME_ID, entriesCaptor.getValue().getFirst().getSchemeId());
        assertEquals(TENANT_ID, entriesCaptor.getValue().getFirst().getTenantId());
        assertEquals(USER_ID, entriesCaptor.getValue().getFirst().getCreatedBy());
        assertEquals(2, result.grants().size());
    }

    @Test
    void replaceGrantsShouldRejectRequestsThatRemoveAllProjectAdministration() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(permissionSchemePort.getPermissionSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(scheme()));
        when(projectReadPort.countActiveProjectsByPermissionSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(1L);
        when(permissionDefinitionPort.getPermissionDefinitionsIncludingSystem(TENANT_ID))
                .thenReturn(List.of(definition(ProjectPermissionKeys.ADMINISTER_PROJECTS), definition(ProjectPermissionKeys.BROWSE_PROJECTS)));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> replaceHandler.handle(new ReplaceProjectPermissionGrantsCommand(
                        PROJECT_ID,
                        TENANT_ID,
                        USER_ID,
                        Set.of("admins"),
                        List.of(new PermissionGrantData(
                                ProjectPermissionKeys.BROWSE_PROJECTS,
                                ProjectPermissionGranteeType.PROJECT_ROLE.name(),
                                "Developers",
                                null
                        ))
                ))
        );

        assertEquals(DomainErrorCode.PROJECT_PERMISSION_DENIED, exception.getErrorCode());
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .leadUserId(99L)
                .permissionSchemeId(SCHEME_ID)
                .build();
    }

    private PermissionSchemeEntity scheme() {
        return PermissionSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Project copy")
                .description("Dedicated permission scheme")
                .build();
    }

    private PermissionDefinitionEntity definition(String permissionKey) {
        return PermissionDefinitionEntity.builder()
                .id((long) permissionKey.hashCode())
                .tenantId(0L)
                .permissionKey(permissionKey)
                .name(permissionKey)
                .description(permissionKey)
                .category("PROJECT")
                .isSystem(true)
                .build();
    }

    private PermissionSchemeEntryEntity entry(Long id, String permissionKey, String granteeType, String granteeRef) {
        return PermissionSchemeEntryEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .schemeId(SCHEME_ID)
                .permissionKey(permissionKey)
                .granteeType(granteeType)
                .granteeRef(granteeRef)
                .build();
    }
}
