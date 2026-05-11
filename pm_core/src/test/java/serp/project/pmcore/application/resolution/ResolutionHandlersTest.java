/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.resolution.command.create.CreateResolutionCommand;
import serp.project.pmcore.application.resolution.command.create.CreateResolutionCommandHandler;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionCommand;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionCommandHandler;
import serp.project.pmcore.application.resolution.command.delete.DeleteResolutionResult;
import serp.project.pmcore.application.resolution.command.update.UpdateResolutionCommand;
import serp.project.pmcore.application.resolution.command.update.UpdateResolutionCommandHandler;
import serp.project.pmcore.application.resolution.query.get.GetResolutionByIdQuery;
import serp.project.pmcore.application.resolution.query.get.GetResolutionByIdQueryHandler;
import serp.project.pmcore.application.resolution.query.list.ListResolutionsQuery;
import serp.project.pmcore.application.resolution.query.list.ListResolutionsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;
import serp.project.pmcore.domain.workitem.service.IResolutionService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolutionHandlersTest {

    private static final Long RESOLUTION_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IResolutionService resolutionService;

    private CreateResolutionCommandHandler createHandler;
    private UpdateResolutionCommandHandler updateHandler;
    private DeleteResolutionCommandHandler deleteHandler;
    private GetResolutionByIdQueryHandler getHandler;
    private ListResolutionsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateResolutionCommandHandler(resolutionService);
        updateHandler = new UpdateResolutionCommandHandler(resolutionService);
        deleteHandler = new DeleteResolutionCommandHandler(resolutionService);
        getHandler = new GetResolutionByIdQueryHandler(resolutionService);
        listHandler = new ListResolutionsQueryHandler(resolutionService);
    }

    @Test
    void createHandlerShouldReturnCreatedResolutionView() {
        ResolutionEntity created = resolution(false);
        when(resolutionService.createResolution(any(ResolutionEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        ResolutionView result = createHandler.handle(new CreateResolutionCommand(
                "Done",
                "Completed work",
                1,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<ResolutionEntity> captor = ArgumentCaptor.forClass(ResolutionEntity.class);
        verify(resolutionService).createResolution(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Done", captor.getValue().getName());
        assertEquals(1, captor.getValue().getSequence());
        assertEquals(RESOLUTION_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedResolutionView() {
        ResolutionEntity updated = resolution(false);
        updated.setName("Fixed");
        updated.setSequence(2);
        when(resolutionService.updateResolution(any(), any(), any(), any())).thenReturn(updated);

        ResolutionView result = updateHandler.handle(new UpdateResolutionCommand(
                RESOLUTION_ID,
                new ResolutionUpdateData("Fixed", true, null, false, 2, true),
                TENANT_ID,
                USER_ID
        ));

        verify(resolutionService).updateResolution(eq(RESOLUTION_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Fixed", result.name());
        assertEquals(2, result.sequence());
    }

    @Test
    void getHandlerShouldMarkSystemResolutionAsReadOnly() {
        ResolutionEntity systemResolution = resolution(true);
        when(resolutionService.getVisibleResolutionById(RESOLUTION_ID, TENANT_ID)).thenReturn(systemResolution);

        ResolutionView result = getHandler.handle(new GetResolutionByIdQuery(RESOLUTION_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateResolutions() {
        when(resolutionService.listVisibleResolutions(eq(TENANT_ID), any(ResolutionListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(resolution(false)), 2L));

        PageView<ResolutionView> result = listHandler.handle(new ListResolutionsQuery(
                TENANT_ID,
                "done",
                false,
                0,
                1,
                "name",
                "DESC"
        ));

        ArgumentCaptor<ResolutionListCriteria> criteriaCaptor = ArgumentCaptor.forClass(ResolutionListCriteria.class);
        verify(resolutionService).listVisibleResolutions(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("done", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        ResolutionEntity deleted = resolution(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(resolutionService.deleteResolution(RESOLUTION_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteResolutionResult result = deleteHandler.handle(new DeleteResolutionCommand(
                RESOLUTION_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private ResolutionEntity resolution(boolean system) {
        return ResolutionEntity.builder()
                .id(RESOLUTION_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name("Done")
                .description("Completed work")
                .sequence(1)
                .isSystem(system)
                .build();
    }
}
