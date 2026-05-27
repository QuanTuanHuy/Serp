/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.settings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkflowSettingsOverviewQueryHandlerTest {

    private static final Long TENANT_ID = 20L;
    private static final Long ACTIVE_WORKFLOW_ID = 100L;
    private static final Long INACTIVE_WORKFLOW_ID = 101L;
    private static final Long ISSUE_TYPE_ID = 200L;
    private static final Long SCHEME_ID = 300L;

    @Mock
    private IWorkflowService workflowService;
    @Mock
    private IWorkflowSchemeService workflowSchemeService;
    @Mock
    private IIssueTypeService issueTypeService;
    @Mock
    private IProjectReadPort projectReadPort;

    private GetWorkflowSettingsOverviewQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetWorkflowSettingsOverviewQueryHandler(
                workflowService,
                workflowSchemeService,
                issueTypeService,
                projectReadPort
        );
    }

    @Test
    void handleShouldReturnWorkflowsSchemesMappingsAndBoundProjects() {
        WorkflowEntity activeWorkflow = workflow(
                ACTIVE_WORKFLOW_ID,
                "software_simplified",
                "Software Simplified Workflow",
                false,
                WorkflowLifecycleState.ACTIVE
        );
        WorkflowEntity inactiveWorkflow = workflow(
                INACTIVE_WORKFLOW_ID,
                "retired_workflow",
                "Retired Workflow",
                true,
                WorkflowLifecycleState.INACTIVE
        );
        WorkflowSchemeEntity scheme = scheme(List.of(item(ISSUE_TYPE_ID, ACTIVE_WORKFLOW_ID)));

        when(workflowService.listVisibleWorkflows(eq(TENANT_ID), any(WorkflowListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(activeWorkflow, inactiveWorkflow), 2));
        when(workflowSchemeService.listVisibleWorkflowSchemes(eq(TENANT_ID), any(WorkflowSchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme), 1));
        when(workflowSchemeService.getVisibleWorkflowSchemeDetailById(SCHEME_ID, TENANT_ID))
                .thenReturn(scheme);
        when(issueTypeService.getVisibleIssueTypesByIds(List.of(ISSUE_TYPE_ID), TENANT_ID))
                .thenReturn(List.of(issueType()));
        when(projectReadPort.getActiveProjectsByWorkflowSchemeIds(List.of(SCHEME_ID), TENANT_ID))
                .thenReturn(List.of(project()));

        WorkflowSettingsOverviewView result = handler.handle(new GetWorkflowSettingsOverviewQuery(TENANT_ID));

        assertEquals(2, result.workflows().size());
        assertEquals(ACTIVE_WORKFLOW_ID, result.workflows().getFirst().id());
        assertEquals(WorkflowLifecycleState.ACTIVE, result.workflows().getFirst().lifecycleState());
        assertEquals(1, result.workflows().getFirst().relatedSchemes().size());
        assertEquals("Scrum 1", result.workflows().getFirst().spaces().getFirst().name());
        assertFalse(result.workflows().getFirst().readOnly());
        assertTrue(result.workflows().get(1).readOnly());

        WorkflowSettingsOverviewView.WorkflowSchemeView schemeView = result.workflowSchemes().getFirst();
        assertEquals(SCHEME_ID, schemeView.id());
        assertEquals(ACTIVE_WORKFLOW_ID, schemeView.defaultWorkflowId());
        assertEquals(1, schemeView.items().size());
        assertEquals("Task", schemeView.items().getFirst().workType().name());
        assertEquals("Software Simplified Workflow", schemeView.items().getFirst().workflow().name());
        assertEquals("SCRUM", schemeView.spaces().getFirst().key());
    }

    private WorkflowEntity workflow(
            Long id,
            String key,
            String name,
            boolean system,
            WorkflowLifecycleState lifecycleState) {
        return WorkflowEntity.builder()
                .id(id)
                .tenantId(system ? 0L : TENANT_ID)
                .workflowKey(key)
                .name(name)
                .description(name + " description")
                .currentPublishedVersionId(lifecycleState == WorkflowLifecycleState.ACTIVE ? 900L : null)
                .draftVersionId(901L)
                .lifecycleState(lifecycleState)
                .isSystem(system)
                .updatedAt(1_700_000L)
                .build();
    }

    private WorkflowSchemeEntity scheme(List<WorkflowSchemeItemEntity> items) {
        return WorkflowSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Software Simplified Workflow Scheme")
                .description("Default workflow scheme")
                .defaultWorkflowId(ACTIVE_WORKFLOW_ID)
                .items(items)
                .build();
    }

    private WorkflowSchemeItemEntity item(Long issueTypeId, Long workflowId) {
        return WorkflowSchemeItemEntity.builder()
                .schemeId(SCHEME_ID)
                .issueTypeId(issueTypeId)
                .workflowId(workflowId)
                .build();
    }

    private IssueTypeEntity issueType() {
        return IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(1)
                .isSystem(false)
                .build();
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(400L)
                .tenantId(TENANT_ID)
                .key("SCRUM")
                .name("Scrum 1")
                .workflowSchemeId(SCHEME_ID)
                .isArchived(false)
                .build();
    }
}
