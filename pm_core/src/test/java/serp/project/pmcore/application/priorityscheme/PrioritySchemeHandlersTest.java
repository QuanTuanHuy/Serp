/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.priorityscheme.command.create.CreatePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.create.CreatePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeResult;
import serp.project.pmcore.application.priorityscheme.command.manageitems.ManagePrioritySchemeItemsCommand;
import serp.project.pmcore.application.priorityscheme.command.manageitems.ManagePrioritySchemeItemsCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.update.UpdatePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.update.UpdatePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.query.get.GetPrioritySchemeByIdQuery;
import serp.project.pmcore.application.priorityscheme.query.get.GetPrioritySchemeByIdQueryHandler;
import serp.project.pmcore.application.priorityscheme.query.list.ListPrioritySchemesQuery;
import serp.project.pmcore.application.priorityscheme.query.list.ListPrioritySchemesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.priority.service.IPriorityService;
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
class PrioritySchemeHandlersTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long PRIORITY_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IPrioritySchemeService prioritySchemeService;
    @Mock
    private IPriorityService priorityService;

    private CreatePrioritySchemeCommandHandler createHandler;
    private UpdatePrioritySchemeCommandHandler updateHandler;
    private DeletePrioritySchemeCommandHandler deleteHandler;
    private ManagePrioritySchemeItemsCommandHandler manageItemsHandler;
    private GetPrioritySchemeByIdQueryHandler getHandler;
    private ListPrioritySchemesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreatePrioritySchemeCommandHandler(prioritySchemeService);
        updateHandler = new UpdatePrioritySchemeCommandHandler(prioritySchemeService);
        deleteHandler = new DeletePrioritySchemeCommandHandler(prioritySchemeService);
        manageItemsHandler = new ManagePrioritySchemeItemsCommandHandler(prioritySchemeService, priorityService);
        getHandler = new GetPrioritySchemeByIdQueryHandler(prioritySchemeService, priorityService);
        listHandler = new ListPrioritySchemesQueryHandler(prioritySchemeService);
    }

    @Test
    void createHandlerShouldReturnCreatedSchemeView() {
        PrioritySchemeEntity created = scheme(false);
        when(prioritySchemeService.createPriorityScheme(any(PrioritySchemeEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        PrioritySchemeView result = createHandler.handle(new CreatePrioritySchemeCommand(
                "Team Managed",
                "Default scheme",
                PRIORITY_ID,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<PrioritySchemeEntity> captor = ArgumentCaptor.forClass(PrioritySchemeEntity.class);
        verify(prioritySchemeService).createPriorityScheme(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Team Managed", captor.getValue().getName());
        assertEquals(SCHEME_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedSchemeView() {
        PrioritySchemeEntity updated = scheme(false);
        updated.setName("Updated Scheme");
        when(prioritySchemeService.updatePriorityScheme(any(), any(), any(), any())).thenReturn(updated);

        PrioritySchemeView result = updateHandler.handle(new UpdatePrioritySchemeCommand(
                SCHEME_ID,
                new PrioritySchemeUpdateData("Updated Scheme", true, null, false, null, false),
                TENANT_ID,
                USER_ID
        ));

        verify(prioritySchemeService).updatePriorityScheme(eq(SCHEME_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Updated Scheme", result.name());
    }

    @Test
    void getHandlerShouldReturnDetailWithItemsAndReadOnlyFlag() {
        PrioritySchemeEntity systemScheme = scheme(true);
        systemScheme.setItems(List.of(item(1L, PRIORITY_ID, 1)));
        when(prioritySchemeService.getVisiblePrioritySchemeDetailById(SCHEME_ID, TENANT_ID)).thenReturn(systemScheme);
        when(priorityService.getVisiblePrioritiesByIds(List.of(PRIORITY_ID), TENANT_ID)).thenReturn(List.of(priority()));

        PrioritySchemeDetailView result = getHandler.handle(new GetPrioritySchemeByIdQuery(SCHEME_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertEquals(1, result.items().size());
        assertEquals("high", result.items().getFirst().priority().priorityKey());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateSchemes() {
        when(prioritySchemeService.listVisiblePrioritySchemes(eq(TENANT_ID), any(PrioritySchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme(false)), 2L));

        PageView<PrioritySchemeView> result = listHandler.handle(new ListPrioritySchemesQuery(
                TENANT_ID,
                "team",
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<PrioritySchemeListCriteria> criteriaCaptor = ArgumentCaptor.forClass(PrioritySchemeListCriteria.class);
        verify(prioritySchemeService).listVisiblePrioritySchemes(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("team", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void manageItemsHandlerShouldReturnUpdatedDetail() {
        PrioritySchemeEntity updated = scheme(false);
        updated.setItems(List.of(item(1L, PRIORITY_ID, 1)));
        when(prioritySchemeService.replacePrioritySchemeItems(SCHEME_ID, List.of(PRIORITY_ID), TENANT_ID, USER_ID))
                .thenReturn(updated);
        when(priorityService.getVisiblePrioritiesByIds(List.of(PRIORITY_ID), TENANT_ID)).thenReturn(List.of(priority()));

        PrioritySchemeDetailView result = manageItemsHandler.handle(new ManagePrioritySchemeItemsCommand(
                SCHEME_ID,
                List.of(PRIORITY_ID),
                TENANT_ID,
                USER_ID
        ));

        assertEquals(1, result.items().size());
        assertEquals(PRIORITY_ID, result.items().getFirst().priorityId());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        PrioritySchemeEntity deleted = scheme(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(prioritySchemeService.deletePriorityScheme(SCHEME_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeletePrioritySchemeResult result = deleteHandler.handle(new DeletePrioritySchemeCommand(
                SCHEME_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private PrioritySchemeEntity scheme(boolean system) {
        return PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name("Team Managed")
                .description("Default scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();
    }

    private PrioritySchemeItemEntity item(Long id, Long priorityId, Integer sequence) {
        return PrioritySchemeItemEntity.builder()
                .id(id)
                .schemeId(SCHEME_ID)
                .priorityId(priorityId)
                .sequence(sequence)
                .build();
    }

    private PriorityEntity priority() {
        return PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .color("#ff0000")
                .sequence(1)
                .isSystem(false)
                .build();
    }
}
