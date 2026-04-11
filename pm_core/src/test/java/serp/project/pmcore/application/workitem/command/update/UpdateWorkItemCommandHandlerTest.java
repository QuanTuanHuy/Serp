/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemConfigurationResolver;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemFieldWriteValidator;
import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.customfield.service.impl.WorkItemCustomFieldResolver;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateWorkItemCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long ISSUE_TYPE_ID = 1001L;

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
    private UpdateWorkItemFieldRulesResolver updateWorkItemFieldRulesResolver;
    @Mock
    private UpdateWorkItemConfigurationResolver updateWorkItemConfigurationResolver;
    @Mock
    private WorkItemCustomFieldResolver workItemCustomFieldResolver;
    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    @Mock
    private IOutboxEventService outboxEventService;
    @Mock
    private JsonUtils jsonUtils;

    private UpdateWorkItemCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateWorkItemCommandHandler(
                new UpdateWorkItemValidator(),
                projectService,
                workItemService,
                workItemAuthorizationSupportService,
                issueSecurityService,
                roleActorSubjectValidator,
                updateWorkItemFieldRulesResolver,
                new UpdateWorkItemFieldWriteValidator(),
                updateWorkItemConfigurationResolver,
                workItemCustomFieldResolver,
                customFieldPort,
                issueTypePort,
                workItemCustomFieldValuePort,
                outboxEventService,
                jsonUtils
        );
    }

    @Test
    void handleShouldUpdateSummaryAndPublishOutboxEvent() {
        UpdateWorkItemCommand command = new UpdateWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                new UpdateWorkItemData(Map.of(WorkItemFieldConstants.SUMMARY, "Updated summary"), Map.of()),
                TENANT_ID,
                USER_ID,
                Set.of("dev-team")
        );
        ProjectEntity project = project(false);
        WorkItemEntity workItem = workItem(ISSUE_TYPE_ID, "Old summary", 77L);
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(workItem.getReporterId())
                .assigneeUserId(workItem.getAssigneeId())
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("dev-team"), workItem.getReporterId(), workItem.getAssigneeId()))
                .thenReturn(actorContext);
        when(updateWorkItemFieldRulesResolver.resolveEditFieldRules(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(new WorkItemFieldRules(
                        Map.of(WorkItemFieldConstants.SUMMARY,
                                new WorkItemFieldPolicy("SYSTEM", WorkItemFieldConstants.SUMMARY, true, false, true)),
                        Map.of()
                ));
        when(updateWorkItemConfigurationResolver.resolvePriorityId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getPriorityId());
        when(updateWorkItemConfigurationResolver.resolveSecurityLevelId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getSecurityLevelId());
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of());
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        UpdateWorkItemResult result = handler.handle(command);

        assertEquals("Updated summary", result.summary());
        assertEquals(List.of(WorkItemFieldConstants.SUMMARY), result.changedFields());

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventService).saveEvent(outboxCaptor.capture());
        assertEquals(EventConstants.WorkItem.EventType.WORK_ITEM_UPDATED, outboxCaptor.getValue().getEventType());
    }

    @Test
    void handleShouldResolveAssigneeAndUpdateWorkItem() {
        UpdateWorkItemCommand command = new UpdateWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                new UpdateWorkItemData(Map.of(WorkItemFieldConstants.ASSIGNEE_ID, 123L), Map.of()),
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = project(false);
        WorkItemEntity workItem = workItem(ISSUE_TYPE_ID, "Old summary", 77L);
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of())
                .reporterUserId(workItem.getReporterId())
                .assigneeUserId(workItem.getAssigneeId())
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of(), workItem.getReporterId(), workItem.getAssigneeId()))
                .thenReturn(actorContext);
        when(updateWorkItemFieldRulesResolver.resolveEditFieldRules(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(new WorkItemFieldRules(
                        Map.of(WorkItemFieldConstants.ASSIGNEE_ID,
                                new WorkItemFieldPolicy("SYSTEM", WorkItemFieldConstants.ASSIGNEE_ID, false, false, true)),
                        Map.of()
                ));
        when(workItemAuthorizationSupportService.resolveAssigneeId(project, 123L, actorContext)).thenReturn(123L);
        when(updateWorkItemConfigurationResolver.resolvePriorityId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getPriorityId());
        when(updateWorkItemConfigurationResolver.resolveSecurityLevelId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getSecurityLevelId());
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of());
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        UpdateWorkItemResult result = handler.handle(command);

        assertEquals(123L, result.assigneeId());
        assertEquals(List.of(WorkItemFieldConstants.ASSIGNEE_ID), result.changedFields());
        verify(roleActorSubjectValidator).validateSubjectExistsForAdd(ProjectRoleActorSubjectType.USER, "123");
    }

    @Test
    void handleShouldPersistCustomFieldUpdates() {
        UpdateWorkItemCommand command = new UpdateWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                new UpdateWorkItemData(Map.of(), Map.of("cf_text", "Updated custom value")),
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = project(false);
        WorkItemEntity workItem = workItem(ISSUE_TYPE_ID, "Old summary", 77L);
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of())
                .reporterUserId(workItem.getReporterId())
                .assigneeUserId(workItem.getAssigneeId())
                .build();
        CustomFieldEntity customField = CustomFieldEntity.builder()
                .id(500L)
                .fieldKey("cf_text")
                .typeKey("text")
                .build();
        WorkItemCustomFieldValueEntity existingValue = WorkItemCustomFieldValueEntity.builder()
                .id(900L)
                .customFieldId(500L)
                .workItemId(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .textValue("Old custom value")
                .build();
        WorkItemCustomFieldValueEntity resolvedValue = WorkItemCustomFieldValueEntity.builder()
                .customFieldId(500L)
                .customFieldContextId(501L)
                .valueType("TEXT")
                .textValue("Updated custom value")
                .sortOrder(0)
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of(), workItem.getReporterId(), workItem.getAssigneeId()))
                .thenReturn(actorContext);
        when(updateWorkItemFieldRulesResolver.resolveEditFieldRules(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(new WorkItemFieldRules(
                        Map.of(),
                        Map.of("cf_text", new WorkItemFieldPolicy("CUSTOM", "cf_text", false, false, true))
                ));
        when(updateWorkItemConfigurationResolver.resolvePriorityId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getPriorityId());
        when(updateWorkItemConfigurationResolver.resolveSecurityLevelId(project, workItem, command.data(), TENANT_ID))
                .thenReturn(workItem.getSecurityLevelId());
        when(workItemCustomFieldValuePort.getActiveValuesByWorkItemId(WORK_ITEM_ID, TENANT_ID)).thenReturn(List.of(existingValue));
        when(customFieldPort.getCustomFieldsByFieldKeys(any())).thenReturn(List.of(customField));
        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(Optional.of(IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .typeKey("task")
                .name("Task")
                .build()));
        when(workItemCustomFieldResolver.resolveCustomFields(eq("task"), any(), any()))
                .thenReturn(new ResolvedCustomFields(List.of(resolvedValue), List.of()));
        when(workItemService.updateWorkItem(workItem, USER_ID)).thenAnswer(invocation -> {
            workItem.setUpdatedAt(1_710_000_000_000L);
            workItem.setUpdatedBy(USER_ID);
            return workItem;
        });
        when(jsonUtils.toJson(any())).thenReturn("{}");

        UpdateWorkItemResult result = handler.handle(command);

        assertEquals(List.of("cf_text"), result.changedFields());
        verify(workItemCustomFieldValuePort).softDeleteByWorkItemIdAndCustomFieldIds(eq(WORK_ITEM_ID), anyCollection(), eq(USER_ID), any());
        verify(workItemCustomFieldValuePort).saveAll(any());
    }

    @Test
    void handleShouldRejectFieldNotWritableOnEditScreen() {
        UpdateWorkItemCommand command = new UpdateWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                new UpdateWorkItemData(Map.of(WorkItemFieldConstants.SUMMARY, "Updated summary"), Map.of()),
                TENANT_ID,
                USER_ID,
                Set.of()
        );
        ProjectEntity project = project(false);
        WorkItemEntity workItem = workItem(ISSUE_TYPE_ID, "Old summary", 77L);
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of())
                .reporterUserId(workItem.getReporterId())
                .assigneeUserId(workItem.getAssigneeId())
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of(), workItem.getReporterId(), workItem.getAssigneeId()))
                .thenReturn(actorContext);
        when(updateWorkItemFieldRulesResolver.resolveEditFieldRules(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(WorkItemFieldRules.empty());

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> handler.handle(command)
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_UPDATE, exception.getErrorCode());
        verify(outboxEventService, never()).saveEvent(any());
    }

    private ProjectEntity project(boolean archived) {
        return ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .isArchived(archived)
                .build();
    }

    private WorkItemEntity workItem(Long issueTypeId, String summary, Long assigneeId) {
        return WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .issueTypeId(issueTypeId)
                .issueNo(1L)
                .key("SERP-1")
                .summary(summary)
                .description("Old description")
                .workflowStepId(100L)
                .statusId(200L)
                .priorityId(300L)
                .assigneeId(assigneeId)
                .reporterId(70L)
                .parentId(null)
                .securityLevelId(null)
                .dueDate(null)
                .rank("0|hzzzzz:")
                .timeOriginalEstimate(3600L)
                .timeRemainingEstimate(3600L)
                .timeSpent(0L)
                .build();
    }
}
