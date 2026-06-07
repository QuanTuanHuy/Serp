/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldMutationService;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldResolver;
import serp.project.pmcore.domain.customfield.service.handler.DateCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.DateTimeCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.GroupCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.JsonCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.MultiSelectCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.NumberCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.SelectCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.TextCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.handler.UserCustomFieldValueHandler;
import serp.project.pmcore.domain.customfield.service.impl.WorkItemCustomFieldResolver;
import serp.project.pmcore.domain.customfield.service.impl.WorkItemCustomFieldMutationService;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldOptionPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.domain.fieldconfig.service.IFieldConfigService;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeScreenSchemeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemePort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.screen.entity.ScreenEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;
import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemeItemPort;
import serp.project.pmcore.domain.screen.port.IScreenSchemePort;
import serp.project.pmcore.domain.screen.port.IScreenTabFieldPort;
import serp.project.pmcore.domain.screen.port.IScreenTabPort;
import serp.project.pmcore.domain.screen.service.IScreenService;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.workitem.service.impl.WorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;
import serp.project.pmcore.domain.workitem.service.impl.WorkItemFieldResolver;
import serp.project.pmcore.domain.notification.service.IWorkItemNotificationOutboxPublisher;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.application.workitem.command.create.support.CreateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateRequiredFieldValidator;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemDraftFactory;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemFieldWriteValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;
import serp.project.pmcore.ui.rest.workitem.dto.response.WorkItemResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateWorkItemCommandHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 1001L;
    private static final String ISSUE_TYPE_KEY = "task";
    private static final Long FIELD_CONFIG_ID = 1101L;
    private static final Long FIELD_CONFIG_SCHEME_ID = 1100L;
    private static final Long ISSUE_TYPE_SCREEN_SCHEME_ID = 1200L;
    private static final Long SCREEN_SCHEME_ID = 1201L;
    private static final Long SCREEN_ID = 1202L;
    private static final Long SCREEN_TAB_ID = 1203L;
    private static final Long WORKFLOW_SCHEME_ID = 1300L;
    private static final Long WORKFLOW_ID = 1301L;
    private static final Long WORKFLOW_VERSION_ID = 1302L;
    private static final Long WORKFLOW_STEP_ID = 1303L;
    private static final Long STATUS_ID = 1304L;
    private static final Long PRIORITY_SCHEME_ID = 1400L;
    private static final Long PRIORITY_ID = 1401L;
    private static final Long CUSTOM_FIELD_ID = 1500L;
    private static final Long CUSTOM_FIELD_CONTEXT_ID = 1501L;

    @Mock
    private CreateWorkItemValidator createWorkItemValidator;
    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IWorkflowSchemePort workflowSchemePort;
    @Mock
    private IWorkflowSchemeItemPort workflowSchemeItemPort;
    @Mock
    private IWorkflowPort workflowPort;
    @Mock
    private IWorkflowVersionPort workflowVersionPort;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IFieldConfigSchemePort fieldConfigSchemePort;
    @Mock
    private IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    @Mock
    private IFieldConfigPort fieldConfigPort;
    @Mock
    private IFieldConfigItemPort fieldConfigItemPort;
    @Mock
    private IIssueTypeScreenSchemePort issueTypeScreenSchemePort;
    @Mock
    private IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    @Mock
    private IScreenSchemePort screenSchemePort;
    @Mock
    private IScreenSchemeItemPort screenSchemeItemPort;
    @Mock
    private IScreenPort screenPort;
    @Mock
    private IScreenTabPort screenTabPort;
    @Mock
    private IScreenTabFieldPort screenTabFieldPort;
    @Mock
    private IScreenService screenService;
    @Mock
    private IFieldConfigService fieldConfigService;
    @Mock
    private IPrioritySchemePort prioritySchemePort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    @Mock
    private IIssueSecuritySchemePort issueSecuritySchemePort;
    @Mock
    private IIssueSecurityLevelPort issueSecurityLevelPort;
    @Mock
    private IPrioritySchemeService prioritySchemeService;
    @Mock
    private IIssueSecurityService issueSecurityService;
    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private ICustomFieldContextPort customFieldContextPort;
    @Mock
    private ICustomFieldOptionPort customFieldOptionPort;
    @Mock
    private ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;
    @Mock
    private IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    @Mock
    private IOutboxEventService outboxEventService;
    @Mock
    private JsonUtils jsonUtils;
    @Mock
    private IWorkItemNotificationOutboxPublisher notificationOutboxPublisher;

    private CreateWorkItemCommandHandler createWorkItemCommandHandler;

    @BeforeEach
    void setUp() {
        IWorkItemFieldResolver workItemFieldResolver = buildWorkItemFieldResolver();

        createWorkItemCommandHandler = new CreateWorkItemCommandHandler(
                createWorkItemValidator,
                projectService,
                workItemService,
                new WorkItemCreateConfigurationResolver(
                        issueTypePort,
                        issueTypeSchemeItemPort,
                        workflowSchemePort,
                        workflowSchemeItemPort,
                        workflowPort,
                        workflowVersionPort,
                        workflowStepPort,
                        prioritySchemeService,
                        issueSecurityService
                ),
                new WorkItemAuthorizationSupportService(projectPermissionEvaluationService),
                buildCustomFieldMutationService(),
                new WorkItemCreateRequiredFieldValidator(),
                new WorkItemDraftFactory(),
                new CreateWorkItemFieldRulesResolver(
                        screenService,
                        workItemFieldResolver
                ),
                new WorkItemFieldWriteValidator(),
                outboxEventService,
                jsonUtils,
                notificationOutboxPublisher
        );
        stubHappyPath(List.of(), List.of(screenField("SYSTEM", "summary")));
    }

    @Test
    void executeShouldCreateWorkItemWhenOptionalFieldIsWritableOnCreate() {
        stubHappyPath(List.of(), List.of(
                screenField("SYSTEM", "summary"),
                screenField("SYSTEM", "due_date")
        ));

        WorkItemResponse response = WorkItemResponse.from(createWorkItemCommandHandler.handle(createCommand(
                "Create task",
                null,
                null,
                null,
                1_700_000_000_000L,
                null,
                null,
                null
        )));

        assertEquals(9000L, response.getId());
        assertEquals("SERP-1", response.getKey());
        assertEquals(1_700_000_000_000L, response.getDueDate());
        verify(notificationOutboxPublisher).publishWorkItemCreatedNotifications(
                any(ProjectEntity.class),
                any(WorkItemEntity.class),
                eq(TENANT_ID),
                eq(USER_ID),
                eq(7000L)
        );
    }

    @Test
    void executeShouldRejectSystemFieldNotWritableOnCreate() {
        CreateWorkItemCommand request = createCommand(
                "Write slice 4 tests",
                null,
                null,
                null,
                1_700_000_000_000L,
                null,
                null,
                null
        );

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> createWorkItemCommandHandler.handle(request)
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE, exception.getErrorCode());
    }

    @Test
    void executeShouldRejectCustomFieldNotWritableOnCreate() {
        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> createWorkItemCommandHandler.handle(createCommand(
                        "Create task",
                        null,
                        null,
                        null,
                        null,
                        null,
                        Map.of("customfield_10001", "value"),
                        null
                ))
        );

        assertEquals(DomainErrorCode.FIELD_NOT_WRITABLE_ON_CREATE, exception.getErrorCode());
    }

    @Test
    void executeShouldPersistDefaultCustomFieldValue() {
        stubHappyPath(
                List.of(),
                List.of(
                        screenField("SYSTEM", "summary"),
                        screenField("CUSTOM", "customfield_10001")
                )
        );

        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(CustomFieldEntity.builder()
                        .id(CUSTOM_FIELD_ID)
                        .fieldKey("customfield_10001")
                        .typeKey("text")
                        .build()));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(CustomFieldContextEntity.builder()
                        .id(CUSTOM_FIELD_CONTEXT_ID)
                        .customFieldId(CUSTOM_FIELD_ID)
                        .build()));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of(CustomFieldContextDefaultValueEntity.builder()
                        .contextId(CUSTOM_FIELD_CONTEXT_ID)
                        .valueType("TEXT")
                        .textValue("Default environment")
                        .sortOrder(0)
                        .build()));
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of());

        createWorkItemCommandHandler.handle(createCommand(
                "Create bug",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        ArgumentCaptor<List<WorkItemCustomFieldValueEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(workItemCustomFieldValuePort).saveAll(captor.capture());

        List<WorkItemCustomFieldValueEntity> savedValues = captor.getValue();
        assertEquals(1, savedValues.size());
        assertEquals(CUSTOM_FIELD_ID, savedValues.getFirst().getCustomFieldId());
        assertEquals(CUSTOM_FIELD_CONTEXT_ID, savedValues.getFirst().getCustomFieldContextId());
        assertEquals("TEXT", savedValues.getFirst().getValueType());
        assertEquals("Default environment", savedValues.getFirst().getTextValue());
        assertEquals(9000L, savedValues.getFirst().getWorkItemId());
    }

    @Test
    void executeShouldRejectAmbiguousCustomFieldContext() {
        stubHappyPath(
                List.of(),
                List.of(
                        screenField("SYSTEM", "summary"),
                        screenField("CUSTOM", "customfield_10001")
                )
        );

        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(customField("customfield_10001", "text")));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(
                        issueTypeContext(CUSTOM_FIELD_CONTEXT_ID, ISSUE_TYPE_KEY),
                        issueTypeContext(CUSTOM_FIELD_CONTEXT_ID + 1, ISSUE_TYPE_KEY)
                ));

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> createWorkItemCommandHandler.handle(createCommand(
                        "Create story",
                        null,
                        null,
                        null,
                        null,
                        null,
                        Map.of("customfield_10001", "value"),
                        null
                ))
        );

        assertEquals(DomainErrorCode.CUSTOM_FIELD_CONTEXT_UNRESOLVABLE, exception.getErrorCode());
    }

    @Test
    void executeShouldRejectInvalidSelectOptionValue() {
        stubHappyPath(
                List.of(),
                List.of(
                        screenField("SYSTEM", "summary"),
                        screenField("CUSTOM", "customfield_10001")
                )
        );

        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(customField("customfield_10001", "select")));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(globalContext(CUSTOM_FIELD_CONTEXT_ID)));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of(customFieldOption(5001L, CUSTOM_FIELD_CONTEXT_ID, "allowed")));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> createWorkItemCommandHandler.handle(createCommand(
                        "Create story",
                        null,
                        null,
                        null,
                        null,
                        null,
                        Map.of("customfield_10001", "invalid-option"),
                        null
                ))
        );

        assertEquals(DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID, exception.getErrorCode());
    }

    @Test
    void executeShouldRejectMissingRequiredCustomFieldWithoutDefault() {
        stubHappyPath(
                List.of(FieldConfigItemEntity.builder()
                        .fieldRefType("CUSTOM")
                        .fieldRef("customfield_10001")
                        .isRequired(true)
                        .isHidden(false)
                        .build()),
                List.of(
                        screenField("SYSTEM", "summary"),
                        screenField("CUSTOM", "customfield_10001")
                )
        );

        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("customfield_10001")))
                .thenReturn(List.of(CustomFieldEntity.builder()
                        .id(CUSTOM_FIELD_ID)
                        .fieldKey("customfield_10001")
                        .typeKey("text")
                        .build()));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, ISSUE_TYPE_KEY))
                .thenReturn(List.of(CustomFieldContextEntity.builder()
                        .id(CUSTOM_FIELD_CONTEXT_ID)
                        .customFieldId(CUSTOM_FIELD_ID)
                        .build()));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of());
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of());

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> createWorkItemCommandHandler.handle(createCommand(
                        "Create story",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ))
        );

        assertEquals(DomainErrorCode.REQUIRED_FIELDS_MISSING, exception.getErrorCode());
    }

    private void stubHappyPath(List<FieldConfigItemEntity> fieldConfigItems,
                               List<ScreenTabFieldEntity> screenFields) {
        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .isArchived(false)
                .issueTypeSchemeId(1000L)
                .workflowSchemeId(WORKFLOW_SCHEME_ID)
                .fieldConfigSchemeId(FIELD_CONFIG_SCHEME_ID)
                .issueTypeScreenSchemeId(ISSUE_TYPE_SCREEN_SCHEME_ID)
                .prioritySchemeId(PRIORITY_SCHEME_ID)
                .build());

        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(Optional.of(IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .typeKey(ISSUE_TYPE_KEY)
                .hierarchyLevel(1)
                .build()));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(1000L, TENANT_ID)).thenReturn(List.of(
                IssueTypeSchemeItemEntity.builder()
                        .issueTypeId(ISSUE_TYPE_ID)
                        .build()
        ));

        when(workflowSchemePort.getWorkflowSchemeById(WORKFLOW_SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(WorkflowSchemeEntity.builder()
                .id(WORKFLOW_SCHEME_ID)
                .defaultWorkflowId(WORKFLOW_ID)
                .build()));
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(WORKFLOW_SCHEME_ID, TENANT_ID)).thenReturn(List.of(
                WorkflowSchemeItemEntity.builder()
                        .issueTypeId(ISSUE_TYPE_ID)
                        .workflowId(WORKFLOW_ID)
                        .build()
        ));
        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .currentPublishedVersionId(WORKFLOW_VERSION_ID)
                .build()));
        when(workflowVersionPort.getWorkflowVersionById(WORKFLOW_VERSION_ID, TENANT_ID)).thenReturn(Optional.of(WorkflowVersionEntity.builder()
                .id(WORKFLOW_VERSION_ID)
                .versionState(WorkflowVersionState.PUBLISHED)
                .build()));
        when(workflowStepPort.getInitialStepByWorkflowVersionId(WORKFLOW_VERSION_ID, TENANT_ID)).thenReturn(Optional.of(WorkflowStepEntity.builder()
                .id(WORKFLOW_STEP_ID)
                .statusId(STATUS_ID)
                .build()));

        when(fieldConfigService.resolveFieldConfigId(FIELD_CONFIG_SCHEME_ID, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(FIELD_CONFIG_ID);
        when(fieldConfigItemPort.getFieldConfigItemsByFieldConfigId(FIELD_CONFIG_ID, TENANT_ID)).thenReturn(fieldConfigItems);

        when(issueTypeScreenSchemePort.getIssueTypeScreenSchemeById(ISSUE_TYPE_SCREEN_SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(IssueTypeScreenSchemeEntity.builder()
                .id(ISSUE_TYPE_SCREEN_SCHEME_ID)
                .defaultScreenSchemeId(SCREEN_SCHEME_ID)
                .build()));
        when(issueTypeScreenSchemeItemPort.getIssueTypeScreenSchemeItemsBySchemeId(ISSUE_TYPE_SCREEN_SCHEME_ID, TENANT_ID)).thenReturn(List.of());
        when(screenSchemePort.getScreenSchemeById(SCREEN_SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(ScreenSchemeEntity.builder()
                .id(SCREEN_SCHEME_ID)
                .defaultScreenId(SCREEN_ID)
                .build()));
        when(screenSchemeItemPort.getScreenSchemeItemsByScreenSchemeId(SCREEN_SCHEME_ID, TENANT_ID)).thenReturn(List.of(
                ScreenSchemeItemEntity.builder()
                        .screenSchemeId(SCREEN_SCHEME_ID)
                        .operationKey("CREATE")
                        .screenId(SCREEN_ID)
                        .build()
        ));
        when(screenPort.getScreenById(SCREEN_ID, TENANT_ID)).thenReturn(Optional.of(ScreenEntity.builder()
                .id(SCREEN_ID)
                .build()));
        when(screenService.resolveScreenIdForOperation(
                any(Long.class),
                any(Long.class),
                eq(ISSUE_TYPE_ID),
                eq("CREATE"),
                eq(TENANT_ID)
        )).thenReturn(SCREEN_ID);
        when(screenService.getScreenTabFieldsByScreenId(SCREEN_ID, TENANT_ID)).thenReturn(screenFields);

        when(prioritySchemeService.resolveDefaultPriorityId(PRIORITY_SCHEME_ID, TENANT_ID)).thenReturn(PRIORITY_ID);

        when(workItemService.getNextIssueNumber(PROJECT_ID, TENANT_ID)).thenReturn(1L);
        when(workItemService.getNextRank(PROJECT_ID, TENANT_ID)).thenReturn("0|hzzzzz:");
        when(workItemService.createWorkItem(any(WorkItemEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenAnswer(invocation -> {
                    WorkItemEntity requestEntity = invocation.getArgument(0);
                    requestEntity.setId(9000L);
                    return requestEntity;
                });
        doAnswer(invocation -> {
            OutboxEventEntity event = invocation.getArgument(0);
            event.setId(7000L);
            return event;
        }).when(outboxEventService).saveEvent(any());
        when(jsonUtils.toJson(any())).thenReturn("{}");
    }

    private ScreenTabFieldEntity screenField(String fieldRefType, String fieldRef) {
        return ScreenTabFieldEntity.builder()
                .screenTabId(SCREEN_TAB_ID)
                .fieldRefType(fieldRefType)
                .fieldRef(fieldRef)
                .build();
    }

    private IWorkItemFieldResolver buildWorkItemFieldResolver() {
        return new WorkItemFieldResolver(fieldConfigItemPort, screenService, fieldConfigService);
    }

    private CreateWorkItemCommand createCommand(String summary,
                                                String description,
                                                Long priorityId,
                                                Long startDate,
                                                Long dueDate,
                                                Long parentId,
                                                Map<String, Object> customFields,
                                                Long securityLevelId) {
        return new CreateWorkItemCommand(
                PROJECT_ID,
                ISSUE_TYPE_ID,
                summary,
                description,
                priorityId,
                null,
                parentId,
                startDate,
                dueDate,
                null,
                securityLevelId,
                customFields,
                TENANT_ID,
                USER_ID,
                Set.of()
        );
    }

    private IWorkItemCustomFieldResolver buildCustomFieldResolver() {
        return new WorkItemCustomFieldResolver(
                customFieldPort,
                customFieldContextPort,
                customFieldOptionPort,
                customFieldContextDefaultValuePort,
                List.of(
                        new TextCustomFieldValueHandler(),
                        new NumberCustomFieldValueHandler(),
                        new DateCustomFieldValueHandler(),
                        new DateTimeCustomFieldValueHandler(),
                        new UserCustomFieldValueHandler(),
                        new GroupCustomFieldValueHandler(),
                        new SelectCustomFieldValueHandler(),
                        new MultiSelectCustomFieldValueHandler(),
                        new JsonCustomFieldValueHandler(jsonUtils)
                )
        );
    }

    private IWorkItemCustomFieldMutationService buildCustomFieldMutationService() {
        return new WorkItemCustomFieldMutationService(
                customFieldPort,
                workItemCustomFieldValuePort,
                buildCustomFieldResolver()
        );
    }

    private CustomFieldEntity customField(String fieldKey, String typeKey) {
        return CustomFieldEntity.builder()
                .id(CUSTOM_FIELD_ID)
                .fieldKey(fieldKey)
                .typeKey(typeKey)
                .build();
    }

    private CustomFieldContextEntity globalContext(Long contextId) {
        return CustomFieldContextEntity.builder()
                .id(contextId)
                .customFieldId(CUSTOM_FIELD_ID)
                .build();
    }

    private CustomFieldContextEntity issueTypeContext(Long contextId, String issueTypeKey) {
        return CustomFieldContextEntity.builder()
                .id(contextId)
                .customFieldId(CUSTOM_FIELD_ID)
                .issueTypeKey(issueTypeKey)
                .build();
    }

    private CustomFieldOptionEntity customFieldOption(Long optionId, Long contextId, String optionKey) {
        return CustomFieldOptionEntity.builder()
                .id(optionId)
                .customFieldContextId(contextId)
                .optionKey(optionKey)
                .value(optionKey)
                .isDisabled(false)
                .build();
    }
}
