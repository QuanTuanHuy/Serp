/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommand;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQuery;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQueryHandler;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQuery;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQueryHandler;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowHandlersTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long DRAFT_VERSION_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IWorkflowService workflowService;

    private CreateWorkflowCommandHandler createHandler;
    private GetWorkflowByIdQueryHandler getHandler;
    private ListWorkflowsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateWorkflowCommandHandler(workflowService);
        getHandler = new GetWorkflowByIdQueryHandler(workflowService);
        listHandler = new ListWorkflowsQueryHandler(workflowService);
    }

    @Test
    void createHandlerShouldReturnCreatedWorkflowView() {
        WorkflowEntity created = workflow(false);
        when(workflowService.createWorkflow(any(WorkflowEntity.class), eq(TENANT_ID), eq(USER_ID))).thenReturn(created);

        WorkflowView result = createHandler.handle(new CreateWorkflowCommand(
                "Team Workflow",
                "Draft",
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<WorkflowEntity> captor = ArgumentCaptor.forClass(WorkflowEntity.class);
        verify(workflowService).createWorkflow(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Team Workflow", captor.getValue().getName());
        assertEquals(WORKFLOW_ID, result.id());
        assertEquals(DRAFT_VERSION_ID, result.draftVersionId());
        assertFalse(result.readOnly());
    }

    @Test
    void getHandlerShouldMarkSystemWorkflowAsReadOnly() {
        WorkflowEntity systemWorkflow = workflow(true);
        when(workflowService.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(systemWorkflow);

        WorkflowView result = getHandler.handle(new GetWorkflowByIdQuery(WORKFLOW_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertTrue(result.isSystem());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateWorkflows() {
        when(workflowService.listVisibleWorkflows(eq(TENANT_ID), any(WorkflowListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(workflow(false)), 2L));

        PageView<WorkflowView> result = listHandler.handle(new ListWorkflowsQuery(
                TENANT_ID,
                "kanban",
                true,
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<WorkflowListCriteria> criteriaCaptor = ArgumentCaptor.forClass(WorkflowListCriteria.class);
        verify(workflowService).listVisibleWorkflows(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("kanban", criteriaCaptor.getValue().getSearch());
        assertEquals(true, criteriaCaptor.getValue().getIsActive());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    private WorkflowEntity workflow(boolean system) {
        return WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .workflowKey("team_workflow")
                .name("Team Workflow")
                .description("Draft")
                .currentPublishedVersionId(system ? 100L : null)
                .draftVersionId(DRAFT_VERSION_ID)
                .lifecycleState(system ? WorkflowLifecycleState.ACTIVE : WorkflowLifecycleState.INACTIVE)
                .isSystem(system)
                .build();
    }
}
