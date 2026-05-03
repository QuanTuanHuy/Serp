/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflowscheme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workflowscheme.command.create.CreateWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.create.CreateWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeResult;
import serp.project.pmcore.application.workflowscheme.command.manageitems.ManageWorkflowSchemeItemsCommand;
import serp.project.pmcore.application.workflowscheme.command.manageitems.ManageWorkflowSchemeItemsCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.update.UpdateWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.update.UpdateWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.query.get.GetWorkflowSchemeByIdQuery;
import serp.project.pmcore.application.workflowscheme.query.get.GetWorkflowSchemeByIdQueryHandler;
import serp.project.pmcore.application.workflowscheme.query.list.ListWorkflowSchemesQuery;
import serp.project.pmcore.application.workflowscheme.query.list.ListWorkflowSchemesQueryHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowSchemeListCriteria;
import serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService;
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
class WorkflowSchemeHandlersTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 11L;
    private static final Long WORKFLOW_ID = 12L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IWorkflowSchemeService workflowSchemeService;
    @Mock
    private IIssueTypeService issueTypeService;
    @Mock
    private IWorkflowService workflowService;

    private CreateWorkflowSchemeCommandHandler createHandler;
    private UpdateWorkflowSchemeCommandHandler updateHandler;
    private DeleteWorkflowSchemeCommandHandler deleteHandler;
    private ManageWorkflowSchemeItemsCommandHandler manageItemsHandler;
    private GetWorkflowSchemeByIdQueryHandler getHandler;
    private ListWorkflowSchemesQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateWorkflowSchemeCommandHandler(workflowSchemeService);
        updateHandler = new UpdateWorkflowSchemeCommandHandler(workflowSchemeService);
        deleteHandler = new DeleteWorkflowSchemeCommandHandler(workflowSchemeService);
        manageItemsHandler = new ManageWorkflowSchemeItemsCommandHandler(workflowSchemeService, issueTypeService, workflowService);
        getHandler = new GetWorkflowSchemeByIdQueryHandler(workflowSchemeService, issueTypeService, workflowService);
        listHandler = new ListWorkflowSchemesQueryHandler(workflowSchemeService);
    }

    @Test
    void createHandlerShouldReturnCreatedSchemeView() {
        WorkflowSchemeEntity created = scheme(false);
        when(workflowSchemeService.createWorkflowScheme(any(WorkflowSchemeEntity.class), eq(TENANT_ID), eq(USER_ID)))
                .thenReturn(created);

        WorkflowSchemeView result = createHandler.handle(new CreateWorkflowSchemeCommand(
                "Team Managed",
                "Default scheme",
                WORKFLOW_ID,
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<WorkflowSchemeEntity> captor = ArgumentCaptor.forClass(WorkflowSchemeEntity.class);
        verify(workflowSchemeService).createWorkflowScheme(captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Team Managed", captor.getValue().getName());
        assertEquals(SCHEME_ID, result.id());
        assertFalse(result.readOnly());
    }

    @Test
    void updateHandlerShouldReturnUpdatedSchemeView() {
        WorkflowSchemeEntity updated = scheme(false);
        updated.setName("Updated Scheme");
        when(workflowSchemeService.updateWorkflowScheme(any(), any(), any(), any())).thenReturn(updated);

        WorkflowSchemeView result = updateHandler.handle(new UpdateWorkflowSchemeCommand(
                SCHEME_ID,
                new WorkflowSchemeUpdateData("Updated Scheme", true, null, false, null, false),
                TENANT_ID,
                USER_ID
        ));

        verify(workflowSchemeService).updateWorkflowScheme(eq(SCHEME_ID), any(), eq(TENANT_ID), eq(USER_ID));
        assertEquals("Updated Scheme", result.name());
    }

    @Test
    void getHandlerShouldReturnDetailWithItemsAndReadOnlyFlag() {
        WorkflowSchemeEntity systemScheme = scheme(true);
        systemScheme.setItems(List.of(item(1L, ISSUE_TYPE_ID, WORKFLOW_ID)));
        when(workflowSchemeService.getVisibleWorkflowSchemeDetailById(SCHEME_ID, TENANT_ID)).thenReturn(systemScheme);
        when(issueTypeService.getVisibleIssueTypesByIds(List.of(ISSUE_TYPE_ID), TENANT_ID)).thenReturn(List.of(issueType()));
        when(workflowService.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(workflow(true));

        WorkflowSchemeDetailView result = getHandler.handle(new GetWorkflowSchemeByIdQuery(SCHEME_ID, TENANT_ID));

        assertTrue(result.readOnly());
        assertEquals(1, result.items().size());
        assertEquals("task", result.items().getFirst().issueType().typeKey());
        assertEquals("Default Workflow", result.items().getFirst().workflow().name());
    }

    @Test
    void listHandlerShouldBuildCriteriaAndPaginateSchemes() {
        when(workflowSchemeService.listVisibleWorkflowSchemes(eq(TENANT_ID), any(WorkflowSchemeListCriteria.class)))
                .thenReturn(new PageResult<>(List.of(scheme(false)), 2L));

        PageView<WorkflowSchemeView> result = listHandler.handle(new ListWorkflowSchemesQuery(
                TENANT_ID,
                "team",
                false,
                0,
                1,
                "name",
                "ASC"
        ));

        ArgumentCaptor<WorkflowSchemeListCriteria> criteriaCaptor = ArgumentCaptor.forClass(WorkflowSchemeListCriteria.class);
        verify(workflowSchemeService).listVisibleWorkflowSchemes(eq(TENANT_ID), criteriaCaptor.capture());
        assertEquals("team", criteriaCaptor.getValue().getSearch());
        assertEquals(false, criteriaCaptor.getValue().getIsSystem());
        assertEquals(2, result.totalItems());
        assertEquals(2, result.totalPages());
    }

    @Test
    void manageItemsHandlerShouldReturnUpdatedDetail() {
        WorkflowSchemeEntity updated = scheme(false);
        updated.setItems(List.of(item(1L, ISSUE_TYPE_ID, WORKFLOW_ID)));
        when(workflowSchemeService.replaceWorkflowSchemeItems(
                eq(SCHEME_ID),
                any(),
                eq(TENANT_ID),
                eq(USER_ID)
        )).thenReturn(updated);
        when(issueTypeService.getVisibleIssueTypesByIds(List.of(ISSUE_TYPE_ID), TENANT_ID)).thenReturn(List.of(issueType()));
        when(workflowService.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(workflow(false));

        WorkflowSchemeDetailView result = manageItemsHandler.handle(new ManageWorkflowSchemeItemsCommand(
                SCHEME_ID,
                List.of(new ManageWorkflowSchemeItemsCommand.WorkflowSchemeItemInput(ISSUE_TYPE_ID, WORKFLOW_ID)),
                TENANT_ID,
                USER_ID
        ));

        assertEquals(1, result.items().size());
        assertEquals(ISSUE_TYPE_ID, result.items().getFirst().issueTypeId());
        assertEquals(WORKFLOW_ID, result.items().getFirst().workflowId());
    }

    @Test
    void deleteHandlerShouldReturnDeleteConfirmation() {
        WorkflowSchemeEntity deleted = scheme(false);
        deleted.setDeletedAt(500L);
        deleted.setUpdatedBy(USER_ID);
        when(workflowSchemeService.deleteWorkflowScheme(SCHEME_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteWorkflowSchemeResult result = deleteHandler.handle(new DeleteWorkflowSchemeCommand(
                SCHEME_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    private WorkflowSchemeEntity scheme(boolean system) {
        return WorkflowSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .name("Team Managed")
                .description("Default scheme")
                .defaultWorkflowId(WORKFLOW_ID)
                .build();
    }

    private WorkflowSchemeItemEntity item(Long id, Long issueTypeId, Long workflowId) {
        return WorkflowSchemeItemEntity.builder()
                .id(id)
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

    private WorkflowEntity workflow(boolean system) {
        return WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .workflowKey("default_workflow")
                .name("Default Workflow")
                .description("Published")
                .currentPublishedVersionId(99L)
                .lifecycleState(WorkflowLifecycleState.ACTIVE)
                .isSystem(system)
                .build();
    }
}
