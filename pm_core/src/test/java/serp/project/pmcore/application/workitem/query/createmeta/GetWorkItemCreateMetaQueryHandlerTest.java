/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.createmeta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.command.create.support.CreateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldOptionPort;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.service.IPrioritySchemeService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.project.service.IProjectComponentService;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkItemCreateMetaQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;
    private static final Long ISSUE_TYPE_SCHEME_ID = 100L;
    private static final Long ISSUE_TYPE_ID = 101L;
    private static final Long PRIORITY_SCHEME_ID = 200L;
    private static final Long PRIORITY_ID = 201L;
    private static final Long SECURITY_SCHEME_ID = 300L;
    private static final Long SECURITY_LEVEL_ID = 301L;
    private static final Long STATUS_ID = 401L;
    private static final Long CUSTOM_FIELD_ID = 501L;
    private static final Long CUSTOM_FIELD_CONTEXT_ID = 502L;

    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService issueTypeSchemeService;
    @Mock
    private serp.project.pmcore.domain.issuetype.port.IIssueTypePort issueTypePort;
    @Mock
    private WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver;
    @Mock
    private IStatusService statusService;
    @Mock
    private IPrioritySchemeService prioritySchemeService;
    @Mock
    private IPriorityPort priorityPort;
    @Mock
    private IIssueSecuritySchemePort issueSecuritySchemePort;
    @Mock
    private IIssueSecurityLevelPort issueSecurityLevelPort;
    @Mock
    private IProjectComponentService projectComponentService;
    @Mock
    private CreateWorkItemFieldRulesResolver createWorkItemFieldRulesResolver;
    @Mock
    private ICustomFieldPort customFieldPort;
    @Mock
    private ICustomFieldContextPort customFieldContextPort;
    @Mock
    private ICustomFieldOptionPort customFieldOptionPort;
    @Mock
    private ICustomFieldContextDefaultValuePort customFieldContextDefaultValuePort;

    private GetWorkItemCreateMetaQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetWorkItemCreateMetaQueryHandler(
                projectService,
                projectPermissionEvaluationService,
                workItemAuthorizationSupportService,
                issueTypeSchemeService,
                issueTypePort,
                workItemCreateConfigurationResolver,
                statusService,
                prioritySchemeService,
                priorityPort,
                issueSecuritySchemePort,
                issueSecurityLevelPort,
                projectComponentService,
                createWorkItemFieldRulesResolver,
                customFieldPort,
                customFieldContextPort,
                customFieldOptionPort,
                customFieldContextDefaultValuePort
        );
    }

    @Test
    void handleShouldReturnResolvedCreateMetadataForSelectedIssueType() {
        stubHappyPath(false, true);

        WorkItemCreateMetaView result = handler.handle(new GetWorkItemCreateMetaQuery(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                null,
                Set.of("devs")
        ));

        assertTrue(result.createAllowed());
        assertEquals(PROJECT_ID, result.project().id());
        assertEquals(ISSUE_TYPE_ID, result.selectedIssueTypeId());
        assertEquals("Task", result.issueTypes().getFirst().name());
        assertEquals("Backlog", result.initialStatus().name());
        assertEquals(PRIORITY_ID, result.defaultPriorityId());
        assertEquals(SECURITY_LEVEL_ID, result.defaultSecurityLevelId());
        assertEquals(1, result.components().size());
        assertTrue(result.systemFields().get("summary").required());
        assertEquals(1, result.customFields().size());
        assertEquals("cf_story_points", result.customFields().getFirst().fieldKey());
        assertEquals(1, result.customFields().getFirst().options().size());
        assertEquals(new BigDecimal("3"), result.customFields().getFirst().defaultValues().getFirst().value());

        verify(projectPermissionEvaluationService).checkPermission(any(), any(), eq(ProjectPermissionKeys.BROWSE_PROJECTS));
        verify(projectPermissionEvaluationService).hasPermission(any(), any(), eq(ProjectPermissionKeys.CREATE_ISSUES));
        verify(projectComponentService).listComponents(eq(PROJECT_ID), eq(TENANT_ID), any(ProjectComponentListCriteria.class));
    }

    @Test
    void handleShouldReturnBlockedMetadataWhenProjectIsArchived() {
        stubHappyPath(true, true);

        WorkItemCreateMetaView result = handler.handle(new GetWorkItemCreateMetaQuery(
                TENANT_ID,
                USER_ID,
                PROJECT_ID,
                ISSUE_TYPE_ID,
                Set.of("devs")
        ));

        assertFalse(result.createAllowed());
        assertEquals("Project is archived.", result.createBlockedReason());
        assertNotNull(result.initialStatus());
    }

    private void stubHappyPath(boolean archived,
                               boolean canCreate) {
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Project")
                .projectTypeKey("software")
                .isArchived(archived)
                .issueTypeSchemeId(ISSUE_TYPE_SCHEME_ID)
                .prioritySchemeId(PRIORITY_SCHEME_ID)
                .issueSecuritySchemeId(SECURITY_SCHEME_ID)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(USER_ID)
                .groupKeys(Set.of("devs"))
                .build();

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("devs"))).thenReturn(actorContext);
        when(projectPermissionEvaluationService.hasPermission(any(), any(), eq(ProjectPermissionKeys.CREATE_ISSUES)))
                .thenReturn(canCreate);

        when(issueTypeSchemeService.getVisibleIssueTypeSchemeDetailById(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenReturn(IssueTypeSchemeEntity.builder()
                        .id(ISSUE_TYPE_SCHEME_ID)
                        .defaultIssueTypeId(ISSUE_TYPE_ID)
                        .items(List.of(IssueTypeSchemeItemEntity.builder()
                                .issueTypeId(ISSUE_TYPE_ID)
                                .sequence(1)
                                .build()))
                        .build());
        when(issueTypePort.getIssueTypesByIdsIncludingSystem(List.of(ISSUE_TYPE_ID), TENANT_ID))
                .thenReturn(List.of(IssueTypeEntity.builder()
                        .id(ISSUE_TYPE_ID)
                        .typeKey("task")
                        .name("Task")
                        .description("Standard work item")
                        .hierarchyLevel(1)
                        .build()));

        when(workItemCreateConfigurationResolver.resolveInitialWorkflowStep(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(WorkflowStepEntity.builder()
                        .id(400L)
                        .statusId(STATUS_ID)
                        .build());
        when(statusService.getVisibleStatusById(STATUS_ID, TENANT_ID)).thenReturn(StatusEntity.builder()
                .id(STATUS_ID)
                .statusKey("backlog")
                .name("Backlog")
                .description("Initial status")
                .categoryId(1L)
                .build());

        when(prioritySchemeService.getVisiblePrioritySchemeDetailById(PRIORITY_SCHEME_ID, TENANT_ID))
                .thenReturn(PrioritySchemeEntity.builder()
                        .id(PRIORITY_SCHEME_ID)
                        .defaultPriorityId(PRIORITY_ID)
                        .items(List.of(PrioritySchemeItemEntity.builder()
                                .priorityId(PRIORITY_ID)
                                .sequence(1)
                                .build()))
                        .build());
        when(priorityPort.getPrioritiesByIdsIncludingSystem(List.of(PRIORITY_ID), TENANT_ID))
                .thenReturn(List.of(PriorityEntity.builder()
                        .id(PRIORITY_ID)
                        .priorityKey("medium")
                        .name("Medium")
                        .color("#B38600")
                        .sequence(1)
                        .build()));

        when(issueSecuritySchemePort.getIssueSecuritySchemeByIdIncludingSystem(SECURITY_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueSecuritySchemeEntity.builder()
                        .id(SECURITY_SCHEME_ID)
                        .defaultLevelId(SECURITY_LEVEL_ID)
                        .build()));
        when(issueSecurityLevelPort.getIssueSecurityLevelsBySchemeIdIncludingSystem(SECURITY_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(IssueSecurityLevelEntity.builder()
                        .id(SECURITY_LEVEL_ID)
                        .name("Internal")
                        .description("Internal access")
                        .build()));

        when(projectComponentService.listComponents(eq(PROJECT_ID), eq(TENANT_ID), any(ProjectComponentListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(ProjectComponentEntity.builder()
                        .id(601L)
                        .projectId(PROJECT_ID)
                        .name("Frontend")
                        .description("Frontend component")
                        .leadUserId(USER_ID)
                        .assigneeType("COMPONENT_LEAD")
                        .build()), 1));

        Map<String, WorkItemFieldPolicy> systemPolicies = new LinkedHashMap<>();
        systemPolicies.put("summary", new WorkItemFieldPolicy("SYSTEM", "summary", true, false, true));
        systemPolicies.put("description", new WorkItemFieldPolicy("SYSTEM", "description", false, false, true));
        Map<String, WorkItemFieldPolicy> customPolicies = new LinkedHashMap<>();
        customPolicies.put("cf_story_points", new WorkItemFieldPolicy("CUSTOM", "cf_story_points", true, false, true));
        when(createWorkItemFieldRulesResolver.resolveCreateFieldRules(project, ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(new WorkItemFieldRules(systemPolicies, customPolicies));

        when(customFieldPort.getCustomFieldsByFieldKeys(List.of("cf_story_points")))
                .thenReturn(List.of(CustomFieldEntity.builder()
                        .id(CUSTOM_FIELD_ID)
                        .fieldKey("cf_story_points")
                        .name("Story points")
                        .description("Estimate size")
                        .typeKey("number")
                        .schemaJson("{}")
                        .build()));
        when(customFieldContextPort.getApplicableCustomFieldContexts(CUSTOM_FIELD_ID, "task"))
                .thenReturn(List.of(CustomFieldContextEntity.builder()
                        .id(CUSTOM_FIELD_CONTEXT_ID)
                        .customFieldId(CUSTOM_FIELD_ID)
                        .name("Task context")
                        .issueTypeKey("task")
                        .build()));
        when(customFieldOptionPort.getCustomFieldOptionsByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of(CustomFieldOptionEntity.builder()
                        .id(701L)
                        .customFieldContextId(CUSTOM_FIELD_CONTEXT_ID)
                        .optionKey("size_3")
                        .value("3")
                        .sequence(1)
                        .isDisabled(false)
                        .build()));
        when(customFieldContextDefaultValuePort.getCustomFieldContextDefaultValuesByContextId(CUSTOM_FIELD_CONTEXT_ID))
                .thenReturn(List.of(CustomFieldContextDefaultValueEntity.builder()
                        .contextId(CUSTOM_FIELD_CONTEXT_ID)
                        .valueType("number")
                        .numberValue(new BigDecimal("3"))
                        .sortOrder(1)
                        .build()));
    }
}
