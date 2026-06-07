/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.issuetype.command.IssueTypeEventPayload;
import serp.project.pmcore.application.issuetype.command.IssueTypeOutboxPublisher;
import serp.project.pmcore.application.issuetype.command.create.CreateIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.create.CreateIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.command.delete.DeleteIssueTypeResult;
import serp.project.pmcore.application.issuetype.command.update.UpdateIssueTypeCommand;
import serp.project.pmcore.application.issuetype.command.update.UpdateIssueTypeCommandHandler;
import serp.project.pmcore.application.issuetype.query.get.GetIssueTypeByIdQuery;
import serp.project.pmcore.application.issuetype.query.get.GetIssueTypeByIdQueryHandler;
import serp.project.pmcore.application.issuetype.query.list.ListIssueTypesQuery;
import serp.project.pmcore.application.issuetype.query.list.ListIssueTypesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTypeHandlersTest {

    private static final Long ISSUE_TYPE_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IIssueTypeService issueTypeService;
    @Mock
    private IssueTypeOutboxPublisher issueTypeOutboxPublisher;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;

    private CreateIssueTypeCommandHandler createHandler;
    private UpdateIssueTypeCommandHandler updateHandler;
    private DeleteIssueTypeCommandHandler deleteHandler;
    private GetIssueTypeByIdQueryHandler getHandler;
    private ListIssueTypesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateIssueTypeCommandHandler(issueTypeService, issueTypeOutboxPublisher);
        updateHandler = new UpdateIssueTypeCommandHandler(issueTypeService, issueTypeOutboxPublisher);
        deleteHandler = new DeleteIssueTypeCommandHandler(issueTypeService, issueTypeOutboxPublisher);
        getHandler = new GetIssueTypeByIdQueryHandler(issueTypeService);
        listHandler = new ListIssueTypesQueryHandler(issueTypeService, projectReadPort, issueTypeSchemeItemPort);
    }

    @Test
    void createHandlerShouldPublishCreatedOutboxEvent() {
        IssueTypeEntity created = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(1)
                .isSystem(false)
                .build();

        when(issueTypeService.createIssueType(any(IssueTypeEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        IssueTypeView result = createHandler.handle(new CreateIssueTypeCommand(
                "task",
                "Task",
                "Default task",
                "https://serp.local/task.svg",
                1,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<IssueTypeEntity> entityCaptor = ArgumentCaptor.forClass(IssueTypeEntity.class);
        verify(issueTypeService).createIssueType(entityCaptor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("task", entityCaptor.getValue().getTypeKey());
        assertEquals("Task", entityCaptor.getValue().getName());

        ArgumentCaptor<IssueTypeEventPayload> payloadCaptor = ArgumentCaptor.forClass(IssueTypeEventPayload.class);
        verify(issueTypeOutboxPublisher).publishIssueTypeCreated(eq(TENANT_ID), payloadCaptor.capture());
        assertEquals(ISSUE_TYPE_ID, payloadCaptor.getValue().issueTypeId());
        assertEquals(USER_ID, payloadCaptor.getValue().performedBy());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldPublishUpdatedOutboxEvent() {
        IssueTypeEntity updated = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(2)
                .isSystem(false)
                .build();

        when(issueTypeService.updateIssueType(any(), any(), any(), any())).thenReturn(updated);

        IssueTypeView result = updateHandler.handle(new UpdateIssueTypeCommand(
                ISSUE_TYPE_ID,
                new IssueTypeUpdateData("Task", true, null, false, null, false, 2, true),
                TENANT_ID,
                USER_ID
        ));

        verify(issueTypeService).updateIssueType(eq(ISSUE_TYPE_ID), any(), eq(TENANT_ID), eq(USER_ID));
        verify(issueTypeOutboxPublisher).publishIssueTypeUpdated(eq(TENANT_ID), any(IssueTypeEventPayload.class));
        assertEquals(2, result.hierarchyLevel());
        assertFalse(result.readOnly());
    }

    @Test
    void deleteHandlerShouldPublishDeletedOutboxEvent() {
        IssueTypeEntity deleted = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(1)
                .isSystem(false)
                .deletedAt(500L)
                .updatedBy(USER_ID)
                .build();

        when(issueTypeService.deleteIssueType(ISSUE_TYPE_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteIssueTypeResult result = deleteHandler.handle(new DeleteIssueTypeCommand(
                ISSUE_TYPE_ID,
                TENANT_ID,
                USER_ID
        ));

        verify(issueTypeOutboxPublisher).publishIssueTypeDeleted(
                eq(TENANT_ID),
                any(IssueTypeEventPayload.class)
        );
        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void getHandlerShouldMarkSystemIssueTypeAsReadOnly() {
        IssueTypeEntity systemIssueType = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(0L)
                .typeKey("bug")
                .name("Bug")
                .hierarchyLevel(1)
                .isSystem(true)
                .build();

        when(issueTypeService.getVisibleIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(systemIssueType);

        IssueTypeView result = getHandler.handle(new GetIssueTypeByIdQuery(ISSUE_TYPE_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldFilterSortAndPaginateVisibleIssueTypes() {
        when(issueTypeService.listVisibleIssueTypes(eq(TENANT_ID), any(IssueTypeListCriteria.class))).thenReturn(new PageResult<>(
                List.of(IssueTypeEntity.builder().id(1L).tenantId(TENANT_ID).typeKey("task").name("Task").hierarchyLevel(1).isSystem(false).build()),
                2L
        ));

        PageView<IssueTypeView> result = listHandler.handle(new ListIssueTypesQuery(
                TENANT_ID,
                "ta",
                null,
                false,
                null,
                0,
                1,
                "name",
                "DESC"
        ));

        ArgumentCaptor<IssueTypeListCriteria> criteriaCaptor = ArgumentCaptor.forClass(IssueTypeListCriteria.class);
        verify(issueTypeService).listVisibleIssueTypes(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("ta", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(0, criteriaCaptor.getValue().getPage());
        assertEquals(1, criteriaCaptor.getValue().getPageSize());
        assertEquals("name", criteriaCaptor.getValue().getSortBy());
        assertEquals("DESC", criteriaCaptor.getValue().getSortDirection());

        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
        assertEquals(1, result.items().size());
        assertEquals("Task", result.items().getFirst().name());
        assertFalse(result.items().getFirst().readOnly());
    }
}
