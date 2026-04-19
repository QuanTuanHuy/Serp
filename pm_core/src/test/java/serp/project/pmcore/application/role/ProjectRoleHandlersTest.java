/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.role.command.ProjectRoleEventPayload;
import serp.project.pmcore.application.role.command.ProjectRoleOutboxPublisher;
import serp.project.pmcore.application.role.command.create.CreateProjectRoleCommand;
import serp.project.pmcore.application.role.command.create.CreateProjectRoleCommandHandler;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleCommand;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleCommandHandler;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleResult;
import serp.project.pmcore.application.role.command.update.UpdateProjectRoleCommand;
import serp.project.pmcore.application.role.command.update.UpdateProjectRoleCommandHandler;
import serp.project.pmcore.application.role.query.get.GetProjectRoleByIdQuery;
import serp.project.pmcore.application.role.query.get.GetProjectRoleByIdQueryHandler;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQuery;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectRoleUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectRoleHandlersTest {

    private static final Long ROLE_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private ProjectRoleOutboxPublisher projectRoleOutboxPublisher;

    private CreateProjectRoleCommandHandler createHandler;
    private UpdateProjectRoleCommandHandler updateHandler;
    private DeleteProjectRoleCommandHandler deleteHandler;
    private GetProjectRoleByIdQueryHandler getHandler;
    private ListProjectRoleQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateProjectRoleCommandHandler(projectRoleService, projectRoleOutboxPublisher);
        updateHandler = new UpdateProjectRoleCommandHandler(projectRoleService, projectRoleOutboxPublisher);
        deleteHandler = new DeleteProjectRoleCommandHandler(projectRoleService, projectRoleOutboxPublisher);
        getHandler = new GetProjectRoleByIdQueryHandler(projectRoleService);
        listHandler = new ListProjectRoleQueryHandler(projectRoleService);
    }

    @Test
    void createHandlerShouldPublishCreatedOutboxEvent() {
        ProjectRoleEntity created = role("Developers", false);
        when(projectRoleService.createProjectRole(any(ProjectRoleEntity.class), eq(TENANT_ID), eq(USER_ID))).thenReturn(created);

        ProjectRoleView result = createHandler.handle(new CreateProjectRoleCommand(
                "Developers",
                "Delivery team",
                TENANT_ID,
                USER_ID
        ));

        verify(projectRoleService).createProjectRole(any(ProjectRoleEntity.class), eq(TENANT_ID), eq(USER_ID));
        ArgumentCaptor<ProjectRoleEventPayload> payloadCaptor = ArgumentCaptor.forClass(ProjectRoleEventPayload.class);
        verify(projectRoleOutboxPublisher).publishCreated(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(ROLE_ID, payloadCaptor.getValue().roleId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());
        assertEquals("Developers", result.name());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldPublishUpdatedOutboxEvent() {
        ProjectRoleEntity updated = role("QA", false);
        when(projectRoleService.updateProjectRole(any(), any(), any(), any())).thenReturn(updated);

        ProjectRoleView result = updateHandler.handle(new UpdateProjectRoleCommand(
                ROLE_ID,
                new ProjectRoleUpdateData("QA", true, null, true),
                TENANT_ID,
                USER_ID
        ));

        verify(projectRoleService).updateProjectRole(eq(ROLE_ID), any(), eq(TENANT_ID), eq(USER_ID));
        verify(projectRoleOutboxPublisher).publishUpdated(eq(TENANT_ID), any(ProjectRoleEventPayload.class));
        assertEquals("QA", result.name());
    }

    @Test
    void deleteHandlerShouldPublishDeletedOutboxEvent() {
        ProjectRoleEntity deleted = role("Users", false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(projectRoleService.deleteProjectRole(ROLE_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteProjectRoleResult result = deleteHandler.handle(new DeleteProjectRoleCommand(
                ROLE_ID,
                TENANT_ID,
                USER_ID
        ));

        verify(projectRoleOutboxPublisher).publishDeleted(eq(TENANT_ID), any(ProjectRoleEventPayload.class));
        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void getHandlerShouldMarkSystemRoleAsReadOnly() {
        ProjectRoleEntity systemRole = role("Administrators", true);
        when(projectRoleService.getProjectRoleByIdIncludingSystem(ROLE_ID, TENANT_ID)).thenReturn(systemRole);

        ProjectRoleView result = getHandler.handle(new GetProjectRoleByIdQuery(ROLE_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateVisibleRoles() {
        when(projectRoleService.listVisibleProjectRoles(eq(TENANT_ID), any(ProjectRoleListCriteria.class))).thenReturn(new PageResult<>(
                List.of(role("Developers", false)),
                2L
        ));

        PageView<ProjectRoleView> result = listHandler.handle(new ListProjectRoleQuery(
                TENANT_ID,
                "dev",
                false,
                0,
                1,
                "name",
                "DESC"
        ));

        ArgumentCaptor<ProjectRoleListCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProjectRoleListCriteria.class);
        verify(projectRoleService).listVisibleProjectRoles(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("dev", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(1, result.items().size());
        assertEquals(2, result.totalItems());
        assertEquals("Developers", result.items().getFirst().name());
        assertFalse(result.items().getFirst().readOnly());
    }

    private ProjectRoleEntity role(String name, boolean system) {
        return ProjectRoleEntity.builder()
                .id(ROLE_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name(name)
                .description(name + " role")
                .isSystem(system)
                .build();
    }
}
