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
import serp.project.pmcore.application.workflow.command.publish.PublishWorkflowCommand;
import serp.project.pmcore.application.workflow.command.publish.PublishWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.query.validate.ValidateWorkflowQuery;
import serp.project.pmcore.application.workflow.query.validate.ValidateWorkflowQueryHandler;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationFinding;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationSeverity;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowValidationHandlersTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IWorkflowService workflowService;

    private ValidateWorkflowQueryHandler validateHandler;
    private PublishWorkflowCommandHandler publishHandler;

    @BeforeEach
    void setUp() {
        validateHandler = new ValidateWorkflowQueryHandler(workflowService);
        publishHandler = new PublishWorkflowCommandHandler(workflowService);
    }

    @Test
    void validateHandlerShouldReturnStructuredValidationView() {
        when(workflowService.validateWorkflow(WORKFLOW_ID, TENANT_ID)).thenReturn(new WorkflowValidationResult(
                List.of(new WorkflowValidationFinding("V-001", WorkflowValidationSeverity.ERROR, "Missing initial step")),
                List.of(new WorkflowValidationFinding("V-004", WorkflowValidationSeverity.WARNING, "Orphan step"))
        ));

        WorkflowValidationView result = validateHandler.handle(new ValidateWorkflowQuery(WORKFLOW_ID, TENANT_ID));

        assertFalse(result.valid());
        assertEquals("V-001", result.errors().getFirst().ruleKey());
        assertEquals("WARNING", result.warnings().getFirst().severity());
    }

    @Test
    void publishHandlerShouldReturnPublishedWorkflowView() {
        when(workflowService.publishWorkflow(WORKFLOW_ID, TENANT_ID, USER_ID)).thenReturn(
                WorkflowEntity.builder()
                        .id(WORKFLOW_ID)
                        .tenantId(TENANT_ID)
                        .workflowKey("team_workflow")
                        .name("Team Workflow")
                        .currentPublishedVersionId(100L)
                        .draftVersionId(null)
                        .lifecycleState(WorkflowLifecycleState.ACTIVE)
                        .isSystem(false)
                        .build()
        );

        WorkflowView result = publishHandler.handle(new PublishWorkflowCommand(WORKFLOW_ID, TENANT_ID, USER_ID));

        assertEquals(100L, result.currentPublishedVersionId());
        assertEquals(WorkflowLifecycleState.ACTIVE, result.lifecycleState());
    }
}
