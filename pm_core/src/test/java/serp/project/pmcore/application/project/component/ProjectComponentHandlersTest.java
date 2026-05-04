/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.component.command.create.CreateProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.create.CreateProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentResult;
import serp.project.pmcore.application.project.component.command.update.UpdateProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.update.UpdateProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.query.get.GetProjectComponentByIdQuery;
import serp.project.pmcore.application.project.component.query.get.GetProjectComponentByIdQueryHandler;
import serp.project.pmcore.application.project.component.query.list.ListProjectComponentsQuery;
import serp.project.pmcore.application.project.component.query.list.ListProjectComponentsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectComponentUpdateData;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectComponentHandlersTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long COMPONENT_ID = 20L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectComponentService projectComponentService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private CreateProjectComponentCommandHandler createHandler;
    private UpdateProjectComponentCommandHandler updateHandler;
    private DeleteProjectComponentCommandHandler deleteHandler;
    private GetProjectComponentByIdQueryHandler getHandler;
    private ListProjectComponentsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateProjectComponentCommandHandler(
                projectService,
                projectComponentService,
                projectPermissionEvaluationService
        );
        updateHandler = new UpdateProjectComponentCommandHandler(
                projectService,
                projectComponentService,
                projectPermissionEvaluationService
        );
        deleteHandler = new DeleteProjectComponentCommandHandler(
                projectService,
                projectComponentService,
                projectPermissionEvaluationService
        );
        getHandler = new GetProjectComponentByIdQueryHandler(
                projectService,
                projectComponentService,
                projectPermissionEvaluationService
        );
        listHandler = new ListProjectComponentsQueryHandler(
                projectService,
                projectComponentService,
                projectPermissionEvaluationService
        );
    }

    @Test
    void createHandlerShouldCheckAdminPermissionAndReturnView() {
        ProjectEntity project = project();
        ProjectComponentEntity created = component("Backend");
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectComponentService.createComponent(any(ProjectComponentEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        ProjectComponentView result = createHandler.handle(new CreateProjectComponentCommand(
                PROJECT_ID,
                "Backend",
                "Backend team",
                99L,
                "PROJECT_DEFAULT",
                TENANT_ID,
                USER_ID,
                Set.of("dev-team")
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(projectComponentService).createComponent(any(ProjectComponentEntity.class), eq(TENANT_ID), eq(USER_ID));
        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(Set.of("dev-team"), contextCaptor.getValue().getGroupKeys());
        assertEquals("Backend", result.name());
    }

    @Test
    void updateHandlerShouldCheckAdminPermissionAndReturnView() {
        ProjectEntity project = project();
        ProjectComponentEntity updated = component("Frontend");
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectComponentService.updateComponent(any(), any(), any(), any(), any())).thenReturn(updated);

        ProjectComponentView result = updateHandler.handle(new UpdateProjectComponentCommand(
                PROJECT_ID,
                COMPONENT_ID,
                new ProjectComponentUpdateData("Frontend", true, null, false, null, false, null, false),
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        verify(projectComponentService).updateComponent(eq(COMPONENT_ID), eq(PROJECT_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Frontend", result.name());
    }

    @Test
    void deleteHandlerShouldCheckAdminPermissionAndReturnDeletedResult() {
        ProjectEntity project = project();
        ProjectComponentEntity deleted = component("Database");
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectComponentService.deleteComponent(COMPONENT_ID, PROJECT_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteProjectComponentResult result = deleteHandler.handle(new DeleteProjectComponentCommand(
                PROJECT_ID,
                COMPONENT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("admins")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.ADMINISTER_PROJECTS)
        );
        assertEquals(true, result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void getHandlerShouldCheckBrowsePermission() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectComponentService.getComponentById(COMPONENT_ID, PROJECT_ID, TENANT_ID)).thenReturn(component("Backend"));

        ProjectComponentView result = getHandler.handle(new GetProjectComponentByIdQuery(
                PROJECT_ID,
                COMPONENT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("devs")
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        assertEquals(COMPONENT_ID, result.id());
    }

    @Test
    void listHandlerShouldCheckBrowsePermissionAndReturnPage() {
        ProjectEntity project = project();
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(projectComponentService.listComponents(eq(PROJECT_ID), eq(TENANT_ID), any(ProjectComponentListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(component("Backend")), 2L));

        PageView<ProjectComponentView> result = listHandler.handle(new ListProjectComponentsQuery(
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                "back",
                0,
                10,
                "name",
                "ASC"
        ));

        verify(projectPermissionEvaluationService).checkPermission(
                eq(ProjectPermissionSubject.from(project)),
                any(ProjectPermissionEvaluationContext.class),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        assertEquals(1, result.items().size());
        assertEquals(2L, result.totalItems());
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build();
    }

    private ProjectComponentEntity component(String name) {
        return ProjectComponentEntity.builder()
                .id(COMPONENT_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .name(name)
                .description(name + " component")
                .assigneeType("PROJECT_DEFAULT")
                .updatedBy(USER_ID)
                .build();
    }
}
