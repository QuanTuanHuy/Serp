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
import serp.project.pmcore.application.workflow.command.addstep.AddWorkflowStepCommand;
import serp.project.pmcore.application.workflow.command.addstep.AddWorkflowStepCommandHandler;
import serp.project.pmcore.application.workflow.command.reordersteps.ReorderWorkflowStepsCommand;
import serp.project.pmcore.application.workflow.command.reordersteps.ReorderWorkflowStepsCommandHandler;
import serp.project.pmcore.application.workflow.command.removestep.DeleteWorkflowStepResult;
import serp.project.pmcore.application.workflow.command.removestep.RemoveWorkflowStepCommand;
import serp.project.pmcore.application.workflow.command.removestep.RemoveWorkflowStepCommandHandler;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowStepHandlersTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long STEP_ID = 11L;
    private static final Long STATUS_ID = 12L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IWorkflowService workflowService;

    private AddWorkflowStepCommandHandler addHandler;
    private RemoveWorkflowStepCommandHandler removeHandler;
    private ReorderWorkflowStepsCommandHandler reorderHandler;

    @BeforeEach
    void setUp() {
        addHandler = new AddWorkflowStepCommandHandler(workflowService);
        removeHandler = new RemoveWorkflowStepCommandHandler(workflowService);
        reorderHandler = new ReorderWorkflowStepsCommandHandler(workflowService);
    }

    @Test
    void addHandlerShouldReturnCreatedWorkflowStepView() {
        WorkflowStepEntity step = step(STEP_ID, 1);
        when(workflowService.addWorkflowStep(WORKFLOW_ID, STATUS_ID, true, false, TENANT_ID, USER_ID)).thenReturn(step);

        WorkflowStepView result = addHandler.handle(new AddWorkflowStepCommand(
                WORKFLOW_ID,
                STATUS_ID,
                true,
                false,
                TENANT_ID,
                USER_ID
        ));

        assertEquals(STEP_ID, result.id());
        assertTrue(result.isInitial());
    }

    @Test
    void removeHandlerShouldReturnDeleteResult() {
        WorkflowStepEntity step = step(STEP_ID, 1);
        step.setDeletedAt(500L);
        step.setUpdatedBy(USER_ID);
        when(workflowService.removeWorkflowStep(WORKFLOW_ID, STEP_ID, TENANT_ID, USER_ID)).thenReturn(step);

        DeleteWorkflowStepResult result = removeHandler.handle(new RemoveWorkflowStepCommand(
                WORKFLOW_ID,
                STEP_ID,
                TENANT_ID,
                USER_ID
        ));

        assertTrue(result.deleted());
        assertEquals(500L, result.deletedAt());
    }

    @Test
    void reorderHandlerShouldReturnOrderedStepViews() {
        when(workflowService.reorderWorkflowSteps(WORKFLOW_ID, List.of(2L, 1L), TENANT_ID, USER_ID))
                .thenReturn(List.of(step(2L, 1), step(1L, 2)));

        List<WorkflowStepView> result = reorderHandler.handle(new ReorderWorkflowStepsCommand(
                WORKFLOW_ID,
                List.of(2L, 1L),
                TENANT_ID,
                USER_ID
        ));

        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).reorderWorkflowSteps(eq(WORKFLOW_ID), captor.capture(), eq(TENANT_ID), eq(USER_ID));
        assertEquals(List.of(2L, 1L), captor.getValue());
        assertEquals(2L, result.getFirst().id());
        assertEquals(1, result.getFirst().stepOrder());
    }

    private WorkflowStepEntity step(Long id, int stepOrder) {
        return WorkflowStepEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(100L)
                .stepKey("in_progress")
                .name("In Progress")
                .statusId(STATUS_ID)
                .stepOrder(stepOrder)
                .isInitial(stepOrder == 1)
                .isTerminal(false)
                .build();
    }
}
