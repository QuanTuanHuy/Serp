/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.blueprint.command.create.CreateProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.create.CreateProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintResult;
import serp.project.pmcore.application.blueprint.command.update.UpdateProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.update.UpdateProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.query.get.GetProjectBlueprintByIdQuery;
import serp.project.pmcore.application.blueprint.query.get.GetProjectBlueprintByIdQueryHandler;
import serp.project.pmcore.application.blueprint.query.list.ListProjectBlueprintsQuery;
import serp.project.pmcore.application.blueprint.query.list.ListProjectBlueprintsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;
import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.blueprint.service.IProjectBlueprintService;
import serp.project.pmcore.domain.shared.enums.SchemeType;
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
class ProjectBlueprintHandlersTest {

    private static final Long BLUEPRINT_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectBlueprintService projectBlueprintService;

    private CreateProjectBlueprintCommandHandler createHandler;
    private UpdateProjectBlueprintCommandHandler updateHandler;
    private DeleteProjectBlueprintCommandHandler deleteHandler;
    private GetProjectBlueprintByIdQueryHandler getHandler;
    private ListProjectBlueprintsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateProjectBlueprintCommandHandler(projectBlueprintService);
        updateHandler = new UpdateProjectBlueprintCommandHandler(projectBlueprintService);
        deleteHandler = new DeleteProjectBlueprintCommandHandler(projectBlueprintService);
        getHandler = new GetProjectBlueprintByIdQueryHandler(projectBlueprintService);
        listHandler = new ListProjectBlueprintsQueryHandler(projectBlueprintService);
    }

    @Test
    void createHandlerShouldReturnCreatedBlueprintView() {
        ProjectBlueprintEntity created = blueprint("Software Template", "software", false);
        when(projectBlueprintService.createBlueprint(any(ProjectBlueprintEntity.class), eq(TENANT_ID), eq(USER_ID))).thenReturn(created);

        ProjectBlueprintView result = createHandler.handle(new CreateProjectBlueprintCommand(
                "Software Template",
                "Default template",
                "software",
                "https://example.com/icon.png",
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<ProjectBlueprintEntity> captor = ArgumentCaptor.forClass(ProjectBlueprintEntity.class);
        verify(projectBlueprintService).createBlueprint(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Software Template", captor.getValue().getName());
        assertEquals("software", captor.getValue().getTypeKey());
        assertEquals(BLUEPRINT_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedBlueprintView() {
        ProjectBlueprintEntity updated = blueprint("Business Template", "business", false);
        when(projectBlueprintService.updateBlueprint(any(), any(), any(), any())).thenReturn(updated);

        ProjectBlueprintView result = updateHandler.handle(new UpdateProjectBlueprintCommand(
                BLUEPRINT_ID,
                new ProjectBlueprintUpdateData("Business Template", true, null, true, null, true),
                TENANT_ID,
                USER_ID
        ));

        verify(projectBlueprintService).updateBlueprint(eq(BLUEPRINT_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Business Template", result.name());
    }

    @Test
    void getHandlerShouldReturnDetailWithSchemeDefaultsAndReadOnlyFlag() {
        ProjectBlueprintEntity systemBlueprint = blueprint("System Template", "software", true);
        BlueprintSchemeDefaultEntity defaultEntity = BlueprintSchemeDefaultEntity.builder()
                .id(100L)
                .schemeType(SchemeType.WORKFLOW)
                .schemeId(200L)
                .build();
        when(projectBlueprintService.getBlueprintByIdIncludingSystemOrThrow(BLUEPRINT_ID, TENANT_ID)).thenReturn(systemBlueprint);
        when(projectBlueprintService.getBlueprintDefaultsIncludingSystem(BLUEPRINT_ID, TENANT_ID)).thenReturn(List.of(defaultEntity));

        ProjectBlueprintDetailView result = getHandler.handle(new GetProjectBlueprintByIdQuery(BLUEPRINT_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertEquals(1, result.schemeDefaults().size());
        assertEquals("WORKFLOW", result.schemeDefaults().getFirst().schemeType());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateBlueprints() {
        when(projectBlueprintService.listBlueprintsIncludingSystem(eq(TENANT_ID), any(ProjectBlueprintListCriteria.class))).thenReturn(new PageResult<>(
                List.of(blueprint("Software Template", "software", false)),
                2L
        ));

        PageView<ProjectBlueprintView> result = listHandler.handle(new ListProjectBlueprintsQuery(
                TENANT_ID,
                "soft",
                "software",
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<ProjectBlueprintListCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProjectBlueprintListCriteria.class);
        verify(projectBlueprintService).listBlueprintsIncludingSystem(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("soft", criteriaCaptor.getValue().getSearch());
        assertEquals("software", criteriaCaptor.getValue().getProjectTypeKey());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(1, result.items().size());
        assertEquals(2, result.totalItems());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        ProjectBlueprintEntity deleted = blueprint("Software Template", "software", false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(projectBlueprintService.deleteBlueprint(BLUEPRINT_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteProjectBlueprintResult result = deleteHandler.handle(new DeleteProjectBlueprintCommand(
                BLUEPRINT_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private ProjectBlueprintEntity blueprint(String name, String typeKey, boolean system) {
        return ProjectBlueprintEntity.builder()
                .id(BLUEPRINT_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name(name)
                .description(name + " description")
                .typeKey(typeKey)
                .avatarUrl("https://example.com/icon.png")
                .isSystem(system)
                .build();
    }
}
