/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priority;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.priority.command.PriorityEventPayload;
import serp.project.pmcore.application.priority.command.PriorityOutboxPublisher;
import serp.project.pmcore.application.priority.command.create.CreatePriorityCommand;
import serp.project.pmcore.application.priority.command.create.CreatePriorityCommandHandler;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityCommand;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityCommandHandler;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityResult;
import serp.project.pmcore.application.priority.command.update.UpdatePriorityCommand;
import serp.project.pmcore.application.priority.command.update.UpdatePriorityCommandHandler;
import serp.project.pmcore.application.priority.query.get.GetPriorityByIdQuery;
import serp.project.pmcore.application.priority.query.get.GetPriorityByIdQueryHandler;
import serp.project.pmcore.application.priority.query.list.ListPrioritiesQuery;
import serp.project.pmcore.application.priority.query.list.ListPrioritiesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.priority.service.IPriorityService;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriorityHandlersTest {

    private static final Long PRIORITY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IPriorityService priorityService;
    @Mock
    private PriorityOutboxPublisher priorityOutboxPublisher;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    
    private CreatePriorityCommandHandler createHandler;
    private UpdatePriorityCommandHandler updateHandler;
    private DeletePriorityCommandHandler deleteHandler;
    private GetPriorityByIdQueryHandler getHandler;
    private ListPrioritiesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreatePriorityCommandHandler(priorityService, priorityOutboxPublisher);
        updateHandler = new UpdatePriorityCommandHandler(priorityService, priorityOutboxPublisher);
        deleteHandler = new DeletePriorityCommandHandler(priorityService, priorityOutboxPublisher);
        getHandler = new GetPriorityByIdQueryHandler(priorityService);
        listHandler = new ListPrioritiesQueryHandler(priorityService, projectReadPort, prioritySchemeItemPort);
    }

    @Test
    void createHandlerShouldPublishCreatedOutboxEvent() {
        PriorityEntity created = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .sequence(1)
                .isSystem(false)
                .build();

        when(priorityService.createPriority(any(PriorityEntity.class), eq(TENANT_ID), eq(USER_ID))).thenReturn(created);

        PriorityView result = createHandler.handle(new CreatePriorityCommand(
                "High",
                "Important",
                "https://serp.local/high.svg",
                "#FFAA00",
                1,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<PriorityEntity> entityCaptor = ArgumentCaptor.forClass(PriorityEntity.class);
        verify(priorityService).createPriority(entityCaptor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("High", entityCaptor.getValue().getName());
        assertEquals("#FFAA00", entityCaptor.getValue().getColor());

        ArgumentCaptor<PriorityEventPayload> payloadCaptor = ArgumentCaptor.forClass(PriorityEventPayload.class);
        verify(priorityOutboxPublisher).publishPriorityCreated(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(PRIORITY_ID, payloadCaptor.getValue().priorityId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldPublishUpdatedOutboxEvent() {
        PriorityEntity updated = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .sequence(2)
                .isSystem(false)
                .build();

        when(priorityService.updatePriority(any(), any(), any(), any())).thenReturn(updated);

        PriorityView result = updateHandler.handle(new UpdatePriorityCommand(
                PRIORITY_ID,
                new PriorityUpdateData("High", true, null, false, null, false, null, false, 2, true),
                TENANT_ID,
                USER_ID
        ));

        verify(priorityService).updatePriority(eq(PRIORITY_ID), any(), eq(TENANT_ID), eq(USER_ID));
        verify(priorityOutboxPublisher).publishPriorityUpdated(eq(TENANT_ID), any(PriorityEventPayload.class));
        assertEquals(2, result.sequence());
        assertFalse(result.readOnly());
    }

    @Test
    void deleteHandlerShouldPublishDeletedOutboxEvent() {
        PriorityEntity deleted = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .sequence(1)
                .isSystem(false)
                .deletedAt(500L)
                .updatedBy(USER_ID)
                .build();

        when(priorityService.deletePriority(PRIORITY_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeletePriorityResult result = deleteHandler.handle(new DeletePriorityCommand(
                PRIORITY_ID,
                TENANT_ID,
                USER_ID
        ));

        verify(priorityOutboxPublisher).publishPriorityDeleted(eq(TENANT_ID), any(PriorityEventPayload.class));
        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void getHandlerShouldMarkSystemPriorityAsReadOnly() {
        PriorityEntity systemPriority = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(0L)
                .priorityKey("highest")
                .name("Highest")
                .sequence(1)
                .isSystem(true)
                .build();

        when(priorityService.getVisiblePriorityById(PRIORITY_ID, TENANT_ID)).thenReturn(systemPriority);

        PriorityView result = getHandler.handle(new GetPriorityByIdQuery(PRIORITY_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateVisiblePriorities() {
        when(priorityService.listVisiblePriorities(eq(TENANT_ID), any(PriorityListCriteria.class))).thenReturn(new PageResult<>(
                List.of(PriorityEntity.builder().id(1L).tenantId(TENANT_ID).priorityKey("high").name("High").sequence(1).isSystem(false).build()),
                2L
        ));

        PageView<PriorityView> result = listHandler.handle(new ListPrioritiesQuery(
                TENANT_ID,
                "hi",
                false,
                null,
                0,
                1,
                "name",
                "DESC"
        ));

        ArgumentCaptor<PriorityListCriteria> criteriaCaptor = ArgumentCaptor.forClass(PriorityListCriteria.class);
        verify(priorityService).listVisiblePriorities(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("hi", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(0, criteriaCaptor.getValue().getPage());
        assertEquals(1, criteriaCaptor.getValue().getPageSize());
        assertEquals("name", criteriaCaptor.getValue().getSortBy());
        assertEquals("DESC", criteriaCaptor.getValue().getSortDirection());

        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
        assertEquals(1, result.items().size());
        assertEquals("High", result.items().getFirst().name());
        assertFalse(result.items().getFirst().readOnly());
    }
}
