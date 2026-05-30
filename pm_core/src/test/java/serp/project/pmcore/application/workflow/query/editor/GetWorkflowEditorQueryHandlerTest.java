/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workflow.query.editor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkflowEditorQueryHandlerTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long DRAFT_VERSION_ID = 101L;
    private static final Long PUBLISHED_VERSION_ID = 100L;

    @Mock
    private IWorkflowService workflowService;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IWorkflowTransitionPort workflowTransitionPort;

    private GetWorkflowEditorQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetWorkflowEditorQueryHandler(
                workflowService,
                workflowStepPort,
                workflowTransitionPort
        );
    }

    @Test
    void handleShouldReturnDraftStepsAndTransitionsWhenDraftExists() {
        WorkflowEntity workflow = workflow(DRAFT_VERSION_ID, PUBLISHED_VERSION_ID, false);
        when(workflowService.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(workflow);
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(step(1L, "To Do", 1), step(2L, "Done", 2)));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(transition(3L, 1L, 2L)));

        WorkflowEditorView result = handler.handle(new GetWorkflowEditorQuery(WORKFLOW_ID, TENANT_ID));

        assertEquals(WORKFLOW_ID, result.workflow().id());
        assertEquals(DRAFT_VERSION_ID, result.versionId());
        assertEquals(WorkflowVersionState.DRAFT, result.versionState());
        assertTrue(result.editable());
        assertEquals(2, result.steps().size());
        assertEquals("To Do", result.steps().getFirst().name());
        assertEquals(1, result.transitions().size());
        assertEquals(2L, result.transitions().getFirst().toStepId());
    }

    @Test
    void handleShouldReturnPublishedVersionReadOnlyWhenDraftIsMissing() {
        WorkflowEntity workflow = workflow(null, PUBLISHED_VERSION_ID, false);
        when(workflowService.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(workflow);
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(step(1L, "To Do", 1)));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(List.of());

        WorkflowEditorView result = handler.handle(new GetWorkflowEditorQuery(WORKFLOW_ID, TENANT_ID));

        assertEquals(PUBLISHED_VERSION_ID, result.versionId());
        assertEquals(WorkflowVersionState.PUBLISHED, result.versionState());
        assertFalse(result.editable());
        assertEquals(1, result.steps().size());
    }

    private WorkflowEntity workflow(Long draftVersionId, Long publishedVersionId, boolean system) {
        return WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .workflowKey("software_simplified")
                .name("Software Simplified Workflow")
                .description("Default project workflow")
                .currentPublishedVersionId(publishedVersionId)
                .draftVersionId(draftVersionId)
                .lifecycleState(WorkflowLifecycleState.ACTIVE)
                .isSystem(system)
                .build();
    }

    private WorkflowStepEntity step(Long id, String name, int order) {
        return WorkflowStepEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .stepKey(name.toLowerCase().replace(" ", "_"))
                .name(name)
                .statusId(id + 100L)
                .stepOrder(order)
                .isInitial(order == 1)
                .isTerminal(order > 1)
                .build();
    }

    private WorkflowTransitionEntity transition(Long id, Long fromStepId, Long toStepId) {
        return WorkflowTransitionEntity.builder()
                .id(id)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .name("Finish")
                .fromStepId(fromStepId)
                .toStepId(toStepId)
                .screenId(null)
                .sequence(1)
                .build();
    }
}
