/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.application.workitem.command.transition.support.TransitionConfigurationResolver;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.service.IWorkItemTransitionRuleEvaluator;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.workitem.service.IWorkItemTransitionAuthorizationService;
import serp.project.pmcore.kernel.utils.JsonUtils;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCustomFieldResolver;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitionWorkItemCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long TRANSITION_ID = 30L;
    private static final Long CURRENT_STEP_ID = 100L;
    private static final Long TARGET_STEP_ID = 101L;
    private static final Long CURRENT_STATUS_ID = 200L;
    private static final Long TARGET_STATUS_ID = 201L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IWorkItemTransitionAuthorizationService workItemTransitionAuthorizationService;
    @Mock
    private TransitionConfigurationResolver transitionConfigurationResolver;
    @Mock
    private IWorkItemTransitionRuleEvaluator workItemTransitionRuleEvaluator;
    @Mock
    private IWorkItemFieldResolver workItemFieldResolver;
    @Mock
    private WorkItemCustomFieldResolver workItemCustomFieldResolver;
    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    @Mock
    private IOutboxEventService outboxEventService;
    @Mock
    private JsonUtils jsonUtils;
    @Mock
    private TransitionWorkItemStatusValidator transitionWorkItemStatusValidator;

    private TransitionWorkItemCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TransitionWorkItemCommandHandler(
                projectService,
                workItemService,
                workItemTransitionAuthorizationService,
                transitionConfigurationResolver,
                workItemTransitionRuleEvaluator,
                workItemFieldResolver,
                workItemCustomFieldResolver,
                customFieldPort,
                workItemCustomFieldValuePort,
                outboxEventService,
                jsonUtils,
                transitionWorkItemStatusValidator
        );
    }

    @Test
    void handleShouldTransitionWorkItemAndPersistOutboxEvent() {
        TransitionWorkItemStatusCommand command = new TransitionWorkItemStatusCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                TRANSITION_ID,
                555L,
                Map.of(),
                TENANT_ID,
                USER_ID,
                Set.of("dev-team")
        );
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .isArchived(false)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .issueTypeId(401L)
                .key("SERP-1")
                .summary("Old summary")
                .workflowStepId(CURRENT_STEP_ID)
                .statusId(CURRENT_STATUS_ID)
                .assigneeId(77L)
                .build();
        ResolvedTransitionExecution execution = buildExecution(null);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(transitionConfigurationResolver.resolve(project, workItem, TRANSITION_ID, TENANT_ID)).thenReturn(execution);
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of());
        when(workItemTransitionAuthorizationService.resolveAssigneeId(eq(project), any(), eq(workItem), any())).thenReturn(77L);
        when(workItemTransitionAuthorizationService.resolveSecurityLevelId(project, workItem, command.toData(), TENANT_ID))
                .thenReturn(null);
        when(workItemTransitionRuleEvaluator.evaluateValidatorsAndResolveResolution(
                eq(execution),
                eq(workItem),
                eq(command.toData()),
                eq(555L),
                eq(Map.of()),
                eq(TENANT_ID)
        )).thenReturn(555L);
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        TransitionWorkItemStatusResult result = handler.handle(command);

        assertEquals(WORK_ITEM_ID, result.id());
        assertEquals(TARGET_STEP_ID, result.workflowStepId());
        assertEquals(TARGET_STATUS_ID, result.statusId());
        assertEquals(555L, result.resolutionId());
        assertEquals(List.of("resolution_id", "workflow_step_id", "status_id"), result.changedFields());

        ArgumentCaptor<OutboxEventEntity> outboxEventCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventService).saveEvent(outboxEventCaptor.capture());
        OutboxEventEntity outboxEvent = outboxEventCaptor.getValue();
        assertEquals(EventConstants.WorkItem.EventType.WORK_ITEM_STATUS_CHANGED, outboxEvent.getEventType());
        assertEquals(WORK_ITEM_ID, outboxEvent.getAggregateId());
        assertEquals(String.valueOf(PROJECT_ID), outboxEvent.getPartitionKey());
    }

    @Test
    void handleShouldRejectFieldPayloadWhenTransitionHasNoScreen() {
        TransitionWorkItemStatusCommand command = new TransitionWorkItemStatusCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                TRANSITION_ID,
                null,
                Map.of("summary", "Updated summary"),
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
                .issueTypeId(401L)
                .key("SERP-1")
                .summary("Old summary")
                .workflowStepId(CURRENT_STEP_ID)
                .statusId(CURRENT_STATUS_ID)
                .build();
        ResolvedTransitionExecution execution = buildExecution(null);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(transitionConfigurationResolver.resolve(project, workItem, TRANSITION_ID, TENANT_ID)).thenReturn(execution);
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of());
        when(workItemTransitionRuleEvaluator.evaluateValidatorsAndResolveResolution(
                eq(execution),
                eq(workItem),
                eq(command.toData()),
                eq(null),
                eq(Map.of()),
                eq(TENANT_ID)
        )).thenReturn(null);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> handler.handle(command)
        );

        assertEquals(DomainErrorCode.TRANSITION_FIELD_INVALID, exception.getErrorCode());
        verify(workItemService, never()).updateWorkItem(any(), anyLong());
    }

    private ResolvedTransitionExecution buildExecution(Long screenId) {
        return new ResolvedTransitionExecution(
                IssueTypeEntity.builder().id(401L).typeKey("task").build(),
                WorkflowVersionEntity.builder().id(501L).build(),
                WorkflowStepEntity.builder().id(CURRENT_STEP_ID).statusId(CURRENT_STATUS_ID).build(),
                WorkflowStepEntity.builder().id(TARGET_STEP_ID).statusId(TARGET_STATUS_ID).build(),
                WorkflowTransitionEntity.builder().id(TRANSITION_ID).name("Done").toStepId(TARGET_STEP_ID).screenId(screenId).build(),
                List.of(),
                StatusEntity.builder().id(TARGET_STATUS_ID).categoryId(701L).build(),
                StatusCategoryEntity.builder().id(701L).key("done").build()
        );
    }
}
