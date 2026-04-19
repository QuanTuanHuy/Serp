/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.ProjectEventPayload;
import serp.project.pmcore.application.project.command.ProjectOutboxPublisher;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.PermissionSeedConstants;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProjectCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long OLD_LEAD_USER_ID = 20L;
    private static final Long NEW_LEAD_USER_ID = 21L;
    private static final Long ROLE_ID = 30L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private UpdateProjectValidator updateProjectValidator;
    @Mock
    private ProjectOutboxPublisher projectOutboxPublisher;

    private UpdateProjectCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateProjectCommandHandler(
                projectService,
                projectRoleService,
                projectRoleActorService,
                projectPermissionEvaluationService,
                updateProjectValidator,
                projectOutboxPublisher
        );
    }

    @Test
    void handleShouldUpdateProjectAndPublishOutboxEvent() {
        ProjectEntity existingProject = projectEntity(OLD_LEAD_USER_ID, false);
        ProjectEntity updatedProject = projectEntity(OLD_LEAD_USER_ID, false);
        updatedProject.setName("SERP Platform Updated");
        updatedProject.setDescription(null);
        updatedProject.setUpdatedBy(USER_ID);
        updatedProject.setUpdatedAt(1_700_000_000_000L);

        UpdateProjectCommand command = new UpdateProjectCommand(
                PROJECT_ID,
                new ProjectUpdateData(
                        "SERP Platform Updated",
                        true,
                        null,
                        false,
                        null,
                        true,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false
                ),
                TENANT_ID,
                USER_ID,
                Set.of("pm-admins")
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(existingProject);
        when(projectService.updateProject(PROJECT_ID, command.data(), TENANT_ID, USER_ID)).thenReturn(updatedProject);

        UpdateProjectResult result = handler.handle(command);

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(existingProject)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );

        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(Set.of("pm-admins"), contextCaptor.getValue().getGroupKeys());

        verify(updateProjectValidator).validate(command, existingProject);
        verify(projectService).updateProject(PROJECT_ID, command.data(), TENANT_ID, USER_ID);

        ArgumentCaptor<ProjectEventPayload> payloadCaptor = ArgumentCaptor.forClass(ProjectEventPayload.class);
        verify(projectOutboxPublisher).publishProjectUpdated(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(PROJECT_ID, payloadCaptor.getValue().projectId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());

        verify(projectRoleService, never()).getProjectRoleByNameIncludingSystem(any(), any());
        assertEquals(PROJECT_ID, result.getId());
        assertEquals("SERP Platform Updated", result.getName());
    }

    @Test
    void handleShouldBootstrapAdminAccessWhenLeadChanges() {
        ProjectEntity existingProject = projectEntity(OLD_LEAD_USER_ID, false);
        ProjectEntity updatedProject = projectEntity(NEW_LEAD_USER_ID, false);

        UpdateProjectCommand command = new UpdateProjectCommand(
                PROJECT_ID,
                new ProjectUpdateData(
                        null,
                        false,
                        null,
                        false,
                        null,
                        false,
                        NEW_LEAD_USER_ID,
                        true,
                        null,
                        false,
                        null,
                        false,
                        null,
                        false
                ),
                TENANT_ID,
                USER_ID,
                Set.of("pm-admins")
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(existingProject);
        when(projectService.updateProject(PROJECT_ID, command.data(), TENANT_ID, USER_ID)).thenReturn(updatedProject);
        when(projectRoleService.getProjectRoleByNameIncludingSystem(
                PermissionSeedConstants.PROJECT_ROLE_ADMINISTRATORS,
                TENANT_ID
        )).thenReturn(Optional.of(ProjectRoleEntity.builder().id(ROLE_ID).build()));
        when(projectPermissionEvaluationService.hasPermission(
                eq(ProjectPermissionSubject.from(updatedProject)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        )).thenReturn(true);

        handler.handle(command);

        verify(projectRoleActorService).assignActorIfAbsent(
                TENANT_ID,
                PROJECT_ID,
                ROLE_ID,
                "USER",
                String.valueOf(NEW_LEAD_USER_ID),
                USER_ID
        );
        verify(projectPermissionEvaluationService).hasPermission(
                eq(ProjectPermissionSubject.from(updatedProject)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
    }

    @Test
    void handleShouldRejectArchivedProject() {
        ProjectEntity archivedProject = projectEntity(OLD_LEAD_USER_ID, true);
        UpdateProjectCommand command = new UpdateProjectCommand(
                PROJECT_ID,
                new ProjectUpdateData(null, false, null, false, null, false, null, false, null, false, null, false, null, false),
                TENANT_ID,
                USER_ID,
                Set.of()
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(archivedProject);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> handler.handle(command)
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, exception.getErrorCode());
        verify(projectPermissionEvaluationService, never()).checkPermission(any(), any(), any());
        verify(updateProjectValidator, never()).validate(any(), any());
    }

    private ProjectEntity projectEntity(Long leadUserId, boolean archived) {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .leadUserId(leadUserId)
                .permissionSchemeId(100L)
                .projectTypeKey("software")
                .isArchived(archived)
                .build();
    }
}
