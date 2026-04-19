/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workflow.command.addtransition.AddWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.addtransition.AddWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.command.removetransition.DeleteWorkflowTransitionResult;
import serp.project.pmcore.application.workflow.command.removetransition.RemoveWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.removetransition.RemoveWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.command.updatetransition.UpdateWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.updatetransition.UpdateWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.query.listtransitions.ListWorkflowTransitionsQuery;
import serp.project.pmcore.application.workflow.query.listtransitions.ListWorkflowTransitionsQueryHandler;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowTransitionHandlersTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long TRANSITION_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IWorkflowService workflowService;

    private AddWorkflowTransitionCommandHandler addHandler;
    private UpdateWorkflowTransitionCommandHandler updateHandler;
    private RemoveWorkflowTransitionCommandHandler removeHandler;
    private ListWorkflowTransitionsQueryHandler listHandler;

    @BeforeEach
    void setUp() {
        addHandler = new AddWorkflowTransitionCommandHandler(workflowService);
        updateHandler = new UpdateWorkflowTransitionCommandHandler(workflowService);
        removeHandler = new RemoveWorkflowTransitionCommandHandler(workflowService);
        listHandler = new ListWorkflowTransitionsQueryHandler(workflowService);
    }

    @Test
    void addHandlerShouldReturnCreatedTransitionView() {
        when(workflowService.addWorkflowTransition(WORKFLOW_ID, "Approve", null, 100L, null, 1, TENANT_ID, USER_ID))
                .thenReturn(transition(TRANSITION_ID, "Approve", null, 100L, 1));

        WorkflowTransitionView result = addHandler.handle(new AddWorkflowTransitionCommand(
                WORKFLOW_ID,
                "Approve",
                null,
                100L,
                null,
                1,
                TENANT_ID,
                USER_ID
        ));

        assertEquals(TRANSITION_ID, result.id());
        assertEquals("Approve", result.name());
    }

    @Test
    void updateHandlerShouldReturnUpdatedTransitionView() {
        when(workflowService.updateWorkflowTransition(WORKFLOW_ID, TRANSITION_ID, "Done", 200L, 3, TENANT_ID, USER_ID))
                .thenReturn(transition(TRANSITION_ID, "Done", 101L, 102L, 3));

        WorkflowTransitionView result = updateHandler.handle(new UpdateWorkflowTransitionCommand(
                WORKFLOW_ID,
                TRANSITION_ID,
                "Done",
                200L,
                3,
                TENANT_ID,
                USER_ID
        ));

        assertEquals("Done", result.name());
        assertEquals(3, result.sequence());
    }

    @Test
    void removeHandlerShouldReturnDeleteResult() {
        WorkflowTransitionEntity deleted = transition(TRANSITION_ID, "Done", 101L, 102L, 3);
        deleted.setDeletedAt(900L);
        deleted.setUpdatedBy(USER_ID);
        when(workflowService.removeWorkflowTransition(WORKFLOW_ID, TRANSITION_ID, TENANT_ID, USER_ID)).thenReturn(deleted);

        DeleteWorkflowTransitionResult result = removeHandler.handle(new RemoveWorkflowTransitionCommand(
                WORKFLOW_ID,
                TRANSITION_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(900L, result.deletedAt());
    }

    @Test
    void listHandlerShouldMapTransitionsToViews() {
        when(workflowService.listWorkflowTransitions(WORKFLOW_ID, 101L, TENANT_ID))
                .thenReturn(List.of(transition(TRANSITION_ID, "Approve", 101L, 102L, 1)));

        List<WorkflowTransitionView> result = listHandler.handle(new ListWorkflowTransitionsQuery(
                WORKFLOW_ID,
                101L,
                TENANT_ID
        ));

        assertEquals(1, result.size());
        assertEquals(TRANSITION_ID, result.getFirst().id());
        assertEquals(101L, result.getFirst().fromStepId());
    }

    private WorkflowTransitionEntity transition(Long id,
                                                String name,
                                                Long fromStepId,
                                                Long toStepId,
                                                Integer sequence) {
        return WorkflowTransitionEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(100L)
                .name(name)
                .fromStepId(fromStepId)
                .toStepId(toStepId)
                .screenId(null)
                .sequence(sequence)
                .build();
    }
}
