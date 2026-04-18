/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.archive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.unarchive.UnarchiveProjectCommand;
import serp.project.pmcore.application.project.command.unarchive.UnarchiveProjectCommandHandler;
import serp.project.pmcore.application.project.command.update.UpdateProjectResult;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectArchiveCommandHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private ArchiveProjectCommandHandler archiveHandler;
    private UnarchiveProjectCommandHandler unarchiveHandler;

    @BeforeEach
    void setUp() {
        archiveHandler = new ArchiveProjectCommandHandler(projectService, projectPermissionEvaluationService);
        unarchiveHandler = new UnarchiveProjectCommandHandler(projectService, projectPermissionEvaluationService);
    }

    @Test
    void archiveHandlerShouldCheckPermissionAndArchiveProject() {
        ProjectEntity existingProject = project(false, null);
        ProjectEntity archivedProject = project(true, 1_700_000_000_000L);
        archivedProject.setUpdatedBy(USER_ID);

        ArchiveProjectCommand command = new ArchiveProjectCommand(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("pm-admins")
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(existingProject);
        when(projectService.archiveProject(PROJECT_ID, TENANT_ID, USER_ID)).thenReturn(archivedProject);

        UpdateProjectResult result = archiveHandler.handle(command);

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(existingProject)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(projectService).archiveProject(PROJECT_ID, TENANT_ID, USER_ID);

        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(Set.of("pm-admins"), contextCaptor.getValue().getGroupKeys());
        assertEquals(true, result.getIsArchived());
        assertEquals(1_700_000_000_000L, result.getArchivedAt());
    }

    @Test
    void unarchiveHandlerShouldCheckPermissionAndUnarchiveProject() {
        ProjectEntity existingProject = project(true, 1_700_000_000_000L);
        ProjectEntity unarchivedProject = project(false, null);
        unarchivedProject.setUpdatedBy(USER_ID);

        UnarchiveProjectCommand command = new UnarchiveProjectCommand(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("pm-admins")
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(existingProject);
        when(projectService.unarchiveProject(PROJECT_ID, TENANT_ID, USER_ID)).thenReturn(unarchivedProject);

        UpdateProjectResult result = unarchiveHandler.handle(command);

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(existingProject)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(projectService).unarchiveProject(PROJECT_ID, TENANT_ID, USER_ID);

        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(Set.of("pm-admins"), contextCaptor.getValue().getGroupKeys());
        assertEquals(false, result.getIsArchived());
        assertNull(result.getArchivedAt());
    }

    private ProjectEntity project(boolean archived, Long archivedAt) {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .projectTypeKey("software")
                .permissionSchemeId(100L)
                .isArchived(archived)
                .archivedAt(archivedAt)
                .build();
    }
}
