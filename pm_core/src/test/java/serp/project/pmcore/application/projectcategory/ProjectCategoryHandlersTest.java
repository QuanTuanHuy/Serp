/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.projectcategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryEventPayload;
import serp.project.pmcore.application.projectcategory.command.ProjectCategoryOutboxPublisher;
import serp.project.pmcore.application.projectcategory.command.create.CreateProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.create.CreateProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryResult;
import serp.project.pmcore.application.projectcategory.command.update.UpdateProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.update.UpdateProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.query.get.GetProjectCategoryByIdQuery;
import serp.project.pmcore.application.projectcategory.query.get.GetProjectCategoryByIdQueryHandler;
import serp.project.pmcore.application.projectcategory.query.list.ListProjectCategoriesQuery;
import serp.project.pmcore.application.projectcategory.query.list.ListProjectCategoriesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCategoryHandlersTest {

    private static final Long CATEGORY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectCategoryService projectCategoryService;
    @Mock
    private ProjectCategoryOutboxPublisher outboxPublisher;

    private CreateProjectCategoryCommandHandler createHandler;
    private UpdateProjectCategoryCommandHandler updateHandler;
    private DeleteProjectCategoryCommandHandler deleteHandler;
    private GetProjectCategoryByIdQueryHandler getHandler;
    private ListProjectCategoriesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateProjectCategoryCommandHandler(projectCategoryService, outboxPublisher);
        updateHandler = new UpdateProjectCategoryCommandHandler(projectCategoryService, outboxPublisher);
        deleteHandler = new DeleteProjectCategoryCommandHandler(projectCategoryService, outboxPublisher);
        getHandler = new GetProjectCategoryByIdQueryHandler(projectCategoryService);
        listHandler = new ListProjectCategoriesQueryHandler(projectCategoryService);
    }

    @Test
    void createHandlerShouldPublishCreatedOutboxEvent() {
        ProjectCategoryEntity created = category("Software");
        when(projectCategoryService.createCategory(any(ProjectCategoryEntity.class), eq(TENANT_ID), eq(USER_ID))).thenReturn(created);

        ProjectCategoryView result = createHandler.handle(new CreateProjectCategoryCommand(
                "Software",
                "Software projects",
                TENANT_ID,
                USER_ID
        ));

        verify(projectCategoryService).createCategory(any(ProjectCategoryEntity.class), eq(TENANT_ID), eq(USER_ID));
        ArgumentCaptor<ProjectCategoryEventPayload> payloadCaptor = ArgumentCaptor.forClass(ProjectCategoryEventPayload.class);
        verify(outboxPublisher).publishCreated(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(CATEGORY_ID, payloadCaptor.getValue().categoryId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());
        assertEquals("Software", result.name());
    }

    @Test
    void updateHandlerShouldPublishUpdatedOutboxEvent() {
        ProjectCategoryEntity updated = category("Business");
        when(projectCategoryService.updateCategory(any(), any(), any(), any())).thenReturn(updated);

        ProjectCategoryView result = updateHandler.handle(new UpdateProjectCategoryCommand(
                CATEGORY_ID,
                new ProjectCategoryUpdateData("Business", true, null, true),
                TENANT_ID,
                USER_ID
        ));

        verify(projectCategoryService).updateCategory(eq(CATEGORY_ID), any(), eq(TENANT_ID), eq(USER_ID));
        verify(outboxPublisher).publishUpdated(eq(TENANT_ID), any(ProjectCategoryEventPayload.class));
        assertEquals("Business", result.name());
    }

    @Test
    void deleteHandlerShouldPublishDeletedOutboxEvent() {
        ProjectCategoryEntity deleted = category("Software");
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(projectCategoryService.deleteCategory(CATEGORY_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteProjectCategoryResult result = deleteHandler.handle(new DeleteProjectCategoryCommand(
                CATEGORY_ID,
                TENANT_ID,
                USER_ID
        ));

        verify(outboxPublisher).publishDeleted(eq(TENANT_ID), any(ProjectCategoryEventPayload.class));
        assertEquals(true, result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void getHandlerShouldReturnMappedCategoryView() {
        when(projectCategoryService.getCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(category("Software"));

        ProjectCategoryView result = getHandler.handle(new GetProjectCategoryByIdQuery(CATEGORY_ID, TENANT_ID));

        assertEquals(CATEGORY_ID, result.id());
        assertEquals("Software", result.name());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateCategories() {
        when(projectCategoryService.listCategories(eq(TENANT_ID), any(ProjectCategoryListCriteria.class))).thenReturn(new PageResult<>(
                List.of(category("Software")),
                2L
        ));

        PageView<ProjectCategoryView> result = listHandler.handle(new ListProjectCategoriesQuery(
                TENANT_ID,
                "soft",
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<ProjectCategoryListCriteria> criteriaCaptor = ArgumentCaptor.forClass(ProjectCategoryListCriteria.class);
        verify(projectCategoryService).listCategories(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("soft", criteriaCaptor.getValue().getSearch());
        assertEquals(1, result.items().size());
        assertEquals(2, result.totalItems());
        assertEquals("Software", result.items().getFirst().name());
    }

    private ProjectCategoryEntity category(String name) {
        return ProjectCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(TENANT_ID)
                .name(name)
                .description(name + " projects")
                .isSystem(false)
                .build();
    }
}
