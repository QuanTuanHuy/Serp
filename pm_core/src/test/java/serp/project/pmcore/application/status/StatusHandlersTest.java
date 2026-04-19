/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.status.command.create.CreateStatusCommand;
import serp.project.pmcore.application.status.command.create.CreateStatusCommandHandler;
import serp.project.pmcore.application.status.command.delete.DeleteStatusCommand;
import serp.project.pmcore.application.status.command.delete.DeleteStatusCommandHandler;
import serp.project.pmcore.application.status.command.delete.DeleteStatusResult;
import serp.project.pmcore.application.status.command.update.UpdateStatusCommand;
import serp.project.pmcore.application.status.command.update.UpdateStatusCommandHandler;
import serp.project.pmcore.application.status.query.get.GetStatusByIdQuery;
import serp.project.pmcore.application.status.query.get.GetStatusByIdQueryHandler;
import serp.project.pmcore.application.status.query.list.ListStatusesQuery;
import serp.project.pmcore.application.status.query.list.ListStatusesQueryHandler;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusHandlersTest {

    private static final Long STATUS_ID = 10L;
    private static final Long CATEGORY_ID = 101L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IStatusService statusService;

    private CreateStatusCommandHandler createHandler;
    private UpdateStatusCommandHandler updateHandler;
    private DeleteStatusCommandHandler deleteHandler;
    private GetStatusByIdQueryHandler getHandler;
    private ListStatusesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateStatusCommandHandler(statusService);
        updateHandler = new UpdateStatusCommandHandler(statusService);
        deleteHandler = new DeleteStatusCommandHandler(statusService);
        getHandler = new GetStatusByIdQueryHandler(statusService);
        listHandler = new ListStatusesQueryHandler(statusService);
    }

    @Test
    void createHandlerShouldReturnCreatedStatusView() {
        StatusEntity created = status(false);
        when(statusService.createStatus(any(StatusEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        StatusView result = createHandler.handle(new CreateStatusCommand(
                "in_progress",
                "In Progress",
                "Doing",
                "https://serp.local/in-progress.svg",
                CATEGORY_ID,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<StatusEntity> captor = ArgumentCaptor.forClass(StatusEntity.class);
        verify(statusService).createStatus(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("in_progress", captor.getValue().getStatusKey());
        assertEquals("In Progress", captor.getValue().getName());
        assertEquals(STATUS_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedStatusView() {
        StatusEntity updated = status(false);
        updated.setName("Doing");
        when(statusService.updateStatus(any(), any(), any(), any())).thenReturn(updated);

        StatusView result = updateHandler.handle(new UpdateStatusCommand(
                STATUS_ID,
                new StatusUpdateData(null, false, "Doing", true, null, false, null, false, null, false),
                TENANT_ID,
                USER_ID
        ));

        verify(statusService).updateStatus(eq(STATUS_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Doing", result.name());
    }

    @Test
    void getHandlerShouldMarkSystemStatusAsReadOnly() {
        StatusEntity systemStatus = status(true);
        when(statusService.getVisibleStatusById(STATUS_ID, TENANT_ID)).thenReturn(systemStatus);

        StatusView result = getHandler.handle(new GetStatusByIdQuery(STATUS_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateStatuses() {
        when(statusService.listVisibleStatuses(eq(TENANT_ID), any(StatusListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(status(false)), 2L));

        PageView<StatusView> result = listHandler.handle(new ListStatusesQuery(
                TENANT_ID,
                "progress",
                CATEGORY_ID,
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<StatusListCriteria> criteriaCaptor = ArgumentCaptor.forClass(StatusListCriteria.class);
        verify(statusService).listVisibleStatuses(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("progress", criteriaCaptor.getValue().getSearch());
        assertEquals(CATEGORY_ID, criteriaCaptor.getValue().getStatusCategoryId());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        StatusEntity deleted = status(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(statusService.deleteStatus(STATUS_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteStatusResult result = deleteHandler.handle(new DeleteStatusCommand(
                STATUS_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private StatusEntity status(boolean system) {
        return StatusEntity.builder()
                .id(STATUS_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .statusKey("in_progress")
                .name("In Progress")
                .description("Doing")
                .iconUrl("https://serp.local/in-progress.svg")
                .categoryId(CATEGORY_ID)
                .isSystem(system)
                .build();
    }
}
