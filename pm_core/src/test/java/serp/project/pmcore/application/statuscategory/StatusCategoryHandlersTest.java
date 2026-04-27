/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.statuscategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.statuscategory.command.create.CreateStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.create.CreateStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryResult;
import serp.project.pmcore.application.statuscategory.command.update.UpdateStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.update.UpdateStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.query.get.GetStatusCategoryByIdQuery;
import serp.project.pmcore.application.statuscategory.query.get.GetStatusCategoryByIdQueryHandler;
import serp.project.pmcore.application.statuscategory.query.list.ListStatusCategoriesQuery;
import serp.project.pmcore.application.statuscategory.query.list.ListStatusCategoriesQueryHandler;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusCategoryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusCategoryHandlersTest {

    private static final Long CATEGORY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IStatusCategoryService statusCategoryService;

    private CreateStatusCategoryCommandHandler createHandler;
    private UpdateStatusCategoryCommandHandler updateHandler;
    private DeleteStatusCategoryCommandHandler deleteHandler;
    private GetStatusCategoryByIdQueryHandler getHandler;
    private ListStatusCategoriesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateStatusCategoryCommandHandler(statusCategoryService);
        updateHandler = new UpdateStatusCategoryCommandHandler(statusCategoryService);
        deleteHandler = new DeleteStatusCategoryCommandHandler(statusCategoryService);
        getHandler = new GetStatusCategoryByIdQueryHandler(statusCategoryService);
        listHandler = new ListStatusCategoriesQueryHandler(statusCategoryService);
    }

    @Test
    void createHandlerShouldReturnCreatedCategoryView() {
        StatusCategoryEntity created = category(false);
        when(statusCategoryService.createStatusCategory(any(StatusCategoryEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        StatusCategoryView result = createHandler.handle(new CreateStatusCategoryCommand(
                "In Progress",
                "indeterminate",
                "yellow",
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<StatusCategoryEntity> captor = ArgumentCaptor.forClass(StatusCategoryEntity.class);
        verify(statusCategoryService).createStatusCategory(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("In Progress", captor.getValue().getName());
        assertEquals("indeterminate", captor.getValue().getKey());
        assertEquals(CATEGORY_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedCategoryView() {
        StatusCategoryEntity updated = category(false);
        updated.setName("Doing");
        when(statusCategoryService.updateStatusCategory(any(), any(), any(), any())).thenReturn(updated);

        StatusCategoryView result = updateHandler.handle(new UpdateStatusCategoryCommand(
                CATEGORY_ID,
                new StatusCategoryUpdateData("Doing", true, null, false, null, false),
                TENANT_ID,
                USER_ID
        ));

        verify(statusCategoryService).updateStatusCategory(eq(CATEGORY_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Doing", result.name());
    }

    @Test
    void getHandlerShouldMarkSystemCategoryAsReadOnly() {
        StatusCategoryEntity systemCategory = category(true);
        when(statusCategoryService.getVisibleStatusCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(systemCategory);

        StatusCategoryView result = getHandler.handle(new GetStatusCategoryByIdQuery(CATEGORY_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateCategories() {
        when(statusCategoryService.listVisibleStatusCategories(eq(TENANT_ID), any(StatusCategoryListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(category(false)), 2L));

        PageView<StatusCategoryView> result = listHandler.handle(new ListStatusCategoriesQuery(
                TENANT_ID,
                "progress",
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<StatusCategoryListCriteria> criteriaCaptor = ArgumentCaptor.forClass(StatusCategoryListCriteria.class);
        verify(statusCategoryService).listVisibleStatusCategories(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("progress", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        StatusCategoryEntity deleted = category(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(statusCategoryService.deleteStatusCategory(CATEGORY_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteStatusCategoryResult result = deleteHandler.handle(new DeleteStatusCategoryCommand(
                CATEGORY_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private StatusCategoryEntity category(boolean system) {
        return StatusCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name("In Progress")
                .key("indeterminate")
                .color("yellow")
                .isSystem(system)
                .build();
    }
}
