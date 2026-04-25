/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowDraftValidatorTest {

    private static final Long WORKFLOW_VERSION_ID = 11L;
    private static final Long TENANT_ID = 20L;

    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IWorkflowTransitionPort workflowTransitionPort;
    @Mock
    private IStatusService statusService;

    private WorkflowDraftValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkflowDraftValidator(workflowStepPort, workflowTransitionPort, statusService);
    }

    @Test
    void validateDraftShouldRejectWhenNoInitialStepExists() {
        WorkflowStepEntity todo = step(1L, 100L, false, true);
        mockDraft(List.of(todo), List.of());
        mockVisibleStatuses(todo);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertFalse(result.isValid());
        assertEquals("V-001", result.errors().getFirst().ruleKey());
    }

    @Test
    void validateDraftShouldRejectWhenMultipleInitialStepsExist() {
        WorkflowStepEntity todo = step(1L, 100L, true, false);
        WorkflowStepEntity doing = step(2L, 101L, true, true);
        mockDraft(List.of(todo, doing), List.of());
        mockVisibleStatuses(todo, doing);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(error -> "V-001".equals(error.ruleKey())));
    }

    @Test
    void validateDraftShouldRejectWhenNoFinalStepExists() {
        WorkflowStepEntity todo = step(1L, 100L, true, false);
        mockDraft(List.of(todo), List.of());
        mockVisibleStatuses(todo);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(error -> "V-002".equals(error.ruleKey())));
    }

    @Test
    void validateDraftShouldRejectUnreachableNonInitialStep() {
        WorkflowStepEntity todo = step(1L, 100L, true, false);
        WorkflowStepEntity doing = step(2L, 101L, false, true);
        mockDraft(List.of(todo, doing), List.of());
        mockVisibleStatuses(todo, doing);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(error ->
                "V-003".equals(error.ruleKey()) && error.message().contains("stepId=2")));
    }

    @Test
    void validateDraftShouldWarnForOrphanStep() {
        WorkflowStepEntity todo = step(1L, 100L, true, true);
        mockDraft(List.of(todo), List.of());
        mockVisibleStatuses(todo);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertTrue(result.isValid());
        assertTrue(result.warnings().stream().anyMatch(warning -> "V-004".equals(warning.ruleKey())));
    }

    @Test
    void validateDraftShouldRejectMissingStatusReference() {
        WorkflowStepEntity todo = step(1L, 100L, true, true);
        mockDraft(List.of(todo), List.of());
        when(statusService.getVisibleStatusById(100L, TENANT_ID)).thenThrow(ResourceNotFoundException.status(100L));

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(error -> "V-005".equals(error.ruleKey())));
    }

    @Test
    void validateDraftShouldWarnForSelfLoopTransition() {
        WorkflowStepEntity todo = step(1L, 100L, true, true);
        WorkflowTransitionEntity selfLoop = transition(10L, 1L, 1L);
        mockDraft(List.of(todo), List.of(selfLoop));
        mockVisibleStatuses(todo);

        WorkflowValidationResult result = validator.validateDraft(WORKFLOW_VERSION_ID, TENANT_ID);

        assertTrue(result.isValid());
        assertTrue(result.warnings().stream().anyMatch(warning -> "V-006".equals(warning.ruleKey())));
    }

    private void mockDraft(List<WorkflowStepEntity> steps, List<WorkflowTransitionEntity> transitions) {
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(WORKFLOW_VERSION_ID, TENANT_ID)).thenReturn(steps);
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(WORKFLOW_VERSION_ID, TENANT_ID))
                .thenReturn(transitions);
    }

    private void mockVisibleStatuses(WorkflowStepEntity... steps) {
        for (WorkflowStepEntity step : steps) {
            when(statusService.getVisibleStatusById(step.getStatusId(), TENANT_ID))
                    .thenReturn(StatusEntity.builder().id(step.getStatusId()).name("Status").build());
        }
    }

    private WorkflowStepEntity step(Long id, Long statusId, boolean initial, boolean terminal) {
        return WorkflowStepEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(WORKFLOW_VERSION_ID)
                .statusId(statusId)
                .stepKey("step_" + id)
                .name("Step " + id)
                .stepOrder(id.intValue())
                .isInitial(initial)
                .isTerminal(terminal)
                .build();
    }

    private WorkflowTransitionEntity transition(Long id, Long fromStepId, Long toStepId) {
        return WorkflowTransitionEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(WORKFLOW_VERSION_ID)
                .name("Transition " + id)
                .fromStepId(fromStepId)
                .toStepId(toStepId)
                .sequence(1)
                .build();
    }
}
