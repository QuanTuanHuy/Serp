/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignWorkItemCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;

    @Mock
    private AssignWorkItemValidator assignWorkItemValidator;
    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IIssueSecurityService issueSecurityService;
    @Mock
    private RoleActorSubjectValidator roleActorSubjectValidator;
    @Mock
    private IOutboxEventService outboxEventService;
    @Mock
    private JsonUtils jsonUtils;

    private AssignWorkItemCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AssignWorkItemCommandHandler(
                assignWorkItemValidator,
                projectService,
                workItemService,
                workItemAuthorizationSupportService,
                issueSecurityService,
                roleActorSubjectValidator,
                outboxEventService,
                jsonUtils
        );
    }

    @Test
    void handleShouldAssignWorkItemAndPersistOutboxEvent() {
        AssignWorkItemCommand command = new AssignWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                123L,
                TENANT_ID,
                USER_ID,
                Set.of("dev-team")
        );
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .issueTypeId(401L)
                .key("SERP-1")
                .statusId(300L)
                .reporterId(70L)
                .assigneeId(77L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(70L)
                .assigneeUserId(77L)
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("dev-team"), 70L, 77L))
                .thenReturn(actorContext);
        when(workItemAuthorizationSupportService.resolveAssigneeId(project, 123L, actorContext)).thenReturn(123L);
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        AssignWorkItemResult result = handler.handle(command);

        assertEquals(WORK_ITEM_ID, result.id());
        assertEquals(123L, result.assigneeId());

        verify(roleActorSubjectValidator).validateSubjectExistsForAdd(any(), any());
        verify(workItemService).updateWorkItem(workItem, USER_ID);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventService).saveEvent(outboxCaptor.capture());
        assertEquals(EventConstants.WorkItem.EventType.WORK_ITEM_ASSIGNED, outboxCaptor.getValue().getEventType());
        assertEquals(String.valueOf(PROJECT_ID), outboxCaptor.getValue().getPartitionKey());
    }

    @Test
    void handleShouldUnassignWorkItemWithoutUserLookup() {
        AssignWorkItemCommand command = new AssignWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                null,
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .key("SERP-1")
                .reporterId(70L)
                .assigneeId(77L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of())
                .reporterUserId(70L)
                .assigneeUserId(77L)
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of(), 70L, 77L))
                .thenReturn(actorContext);
        when(workItemAuthorizationSupportService.resolveAssigneeId(project, null, actorContext)).thenReturn(null);
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        AssignWorkItemResult result = handler.handle(command);

        assertEquals(WORK_ITEM_ID, result.id());
        assertEquals(null, result.assigneeId());
        verify(roleActorSubjectValidator, never()).validateSubjectExistsForAdd(any(), any());
        verify(outboxEventService).saveEvent(any());
    }

    @Test
    void handleShouldSkipPersistenceWhenAssignmentUnchanged() {
        AssignWorkItemCommand command = new AssignWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                77L,
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .key("SERP-1")
                .reporterId(70L)
                .assigneeId(77L)
                .updatedAt(1_700_000_000_000L)
                .updatedBy(55L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of())
                .reporterUserId(70L)
                .assigneeUserId(77L)
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of(), 70L, 77L))
                .thenReturn(actorContext);
        when(workItemAuthorizationSupportService.resolveAssigneeId(project, 77L, actorContext)).thenReturn(77L);

        AssignWorkItemResult result = handler.handle(command);

        assertEquals(77L, result.assigneeId());
        assertEquals(1_700_000_000_000L, result.updatedAt());
        verify(workItemService, never()).updateWorkItem(any(), any());
        verify(outboxEventService, never()).saveEvent(any());
    }

    @Test
    void handleShouldRejectWhenWorkItemDoesNotBelongToProject() {
        AssignWorkItemCommand command = new AssignWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                123L,
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(false)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(999L)
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);

        assertThrows(ResourceNotFoundException.class, () -> handler.handle(command));
        verify(outboxEventService, never()).saveEvent(any());
    }
}
