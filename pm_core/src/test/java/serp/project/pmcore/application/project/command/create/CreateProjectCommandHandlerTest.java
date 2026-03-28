/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.constant.PermissionSeedConstants;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningRequest;
import serp.project.pmcore.domain.dto.project.ProjectProvisioningResult;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;
import serp.project.pmcore.domain.enums.ProvisioningMode;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.validator.ProjectSchemeCompatibilityValidator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProjectCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long LEAD_USER_ID = 20L;
    private static final Long ROLE_ID = 30L;

    @Mock
    private CreateProjectValidator createProjectValidator;
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private ISchemeProvisioningService schemeProvisioningService;
    @Mock
    private ProjectSchemeCompatibilityValidator projectSchemeCompatibilityValidator;

    private CreateProjectCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateProjectCommandHandler(
                createProjectValidator,
                projectService,
                projectRoleService,
                projectRoleActorService,
                schemeProvisioningService,
                projectSchemeCompatibilityValidator
        );
    }

    @Test
    void handleShouldCreateProjectUsingCommandPayload() {
        CreateProjectCommand command = new CreateProjectCommand(
                "SERP Platform",
                "SERP",
                "Project management core",
                "software",
                LEAD_USER_ID,
                40L,
                50L,
                "https://serp.local/project",
                60L,
                100L,
                101L,
                102L,
                103L,
                104L,
                105L,
                106L,
                107L,
                ProvisioningMode.SHARED_FROM_EXISTING,
                TENANT_ID,
                USER_ID
        );

        ProjectEntity savedProject = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key(command.key())
                .leadUserId(command.leadUserId())
                .build();
        ProjectEntity finalProject = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key(command.key())
                .name(command.name())
                .description(command.description())
                .url(command.url())
                .leadUserId(command.leadUserId())
                .avatarId(command.avatarId())
                .categoryId(command.categoryId())
                .projectTypeKey(command.projectTypeKey())
                .issueTypeSchemeId(command.issueTypeSchemeId())
                .workflowSchemeId(command.workflowSchemeId())
                .fieldConfigSchemeId(command.fieldConfigSchemeId())
                .issueTypeScreenSchemeId(command.issueTypeScreenSchemeId())
                .permissionSchemeId(command.permissionSchemeId())
                .notificationSchemeId(command.notificationSchemeId())
                .prioritySchemeId(command.prioritySchemeId())
                .issueSecuritySchemeId(command.issueSecuritySchemeId())
                .createdAt(1_700_000_000_000L)
                .createdBy(USER_ID)
                .updatedAt(1_700_000_000_100L)
                .updatedBy(USER_ID)
                .build();

        when(projectService.createProject(any(ProjectEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(savedProject);
        when(schemeProvisioningService.provisionProjectSchemes(eq(savedProject), any(ProjectProvisioningRequest.class)))
                .thenReturn(ProjectProvisioningResult.builder()
                        .effectiveBindings(command.toSchemeBindings())
                        .build());
        when(projectService.saveProject(savedProject, USER_ID)).thenReturn(finalProject);
        when(projectRoleService.getProjectRoleByNameIncludingSystem(
                PermissionSeedConstants.PROJECT_ROLE_ADMINISTRATORS,
                TENANT_ID
        )).thenReturn(Optional.of(ProjectRoleEntity.builder().id(ROLE_ID).build()));

        CreateProjectResult result = handler.handle(command);

        ArgumentCaptor<ProjectEntity> entityCaptor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectService).createProject(entityCaptor.capture(), eq(TENANT_ID), eq(USER_ID));

        ProjectEntity requestedProject = entityCaptor.getValue();
        assertEquals(command.key(), requestedProject.getKey());
        assertEquals(command.name(), requestedProject.getName());
        assertEquals(command.description(), requestedProject.getDescription());
        assertEquals(command.projectTypeKey(), requestedProject.getProjectTypeKey());

        ArgumentCaptor<ProjectProvisioningRequest> provisioningRequestCaptor = ArgumentCaptor.forClass(ProjectProvisioningRequest.class);
        verify(schemeProvisioningService).provisionProjectSchemes(eq(savedProject), provisioningRequestCaptor.capture());

        ProjectProvisioningRequest provisioningRequest = provisioningRequestCaptor.getValue();
        assertEquals(TENANT_ID, provisioningRequest.getTenantId());
        assertEquals(USER_ID, provisioningRequest.getUserId());
        assertEquals(PROJECT_ID, provisioningRequest.getProjectId());
        assertEquals(command.blueprintId(), provisioningRequest.getBlueprintId());
        assertEquals(command.provisioningMode(), provisioningRequest.getProvisioningMode());
        assertEquals(command.issueTypeSchemeId(), provisioningRequest.getRequestedSchemeBindings().getIssueTypeSchemeId());

        verify(createProjectValidator).validate(command);
        verify(projectSchemeCompatibilityValidator).validate(savedProject, TENANT_ID);
        verify(projectRoleActorService).assignActorIfAbsent(
                TENANT_ID,
                PROJECT_ID,
                ROLE_ID,
                "USER",
                String.valueOf(LEAD_USER_ID),
                USER_ID
        );

        assertEquals(PROJECT_ID, result.getId());
        assertEquals(command.key(), result.getKey());
        assertEquals(command.issueSecuritySchemeId(), result.getIssueSecuritySchemeId());
    }
}
