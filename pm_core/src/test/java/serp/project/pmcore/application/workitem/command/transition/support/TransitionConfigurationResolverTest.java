/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionSubjectContext;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitionConfigurationResolverTest {

    private static final Long TENANT_ID = 1L;
    private static final Long WORKFLOW_ID = 10L;
    private static final Long PUBLISHED_VERSION_ID = 200L;
    private static final Long OLD_VERSION_ID = 100L;
    private static final Long WORK_ITEM_ID = 300L;
    private static final Long ISSUE_TYPE_ID = 400L;
    private static final Long WORKFLOW_SCHEME_ID = 500L;
    private static final Long OLD_STEP_ID = 600L;
    private static final Long CURRENT_STEP_ID = 601L;
    private static final Long TARGET_STEP_ID = 602L;
    private static final Long CURRENT_STATUS_ID = 700L;
    private static final Long TARGET_STATUS_ID = 701L;
    private static final Long STATUS_CATEGORY_ID = 800L;
    private static final Long TRANSITION_ID = 900L;

    @Mock
    private IIssueTypeService issueTypeService;
    @Mock
    private IWorkflowService workflowService;
    @Mock
    private IStatusService statusService;
    @Mock
    private IWorkflowVersionPort workflowVersionPort;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IWorkflowTransitionPort workflowTransitionPort;
    @Mock
    private IWorkflowTransitionRulePort workflowTransitionRulePort;

    private TransitionConfigurationResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TransitionConfigurationResolver(
                issueTypeService,
                workflowService,
                statusService,
                workflowVersionPort,
                workflowStepPort,
                workflowTransitionPort,
                workflowTransitionRulePort
        );
    }

    @Test
    void listAvailableTransitionsShouldRemapStaleWorkItemStepByStatusInPublishedVersion() {
        TransitionSubjectContext context = new TransitionSubjectContext(
                1L,
                WORKFLOW_SCHEME_ID,
                WORK_ITEM_ID,
                ISSUE_TYPE_ID,
                OLD_STEP_ID,
                CURRENT_STATUS_ID
        );
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .currentPublishedVersionId(PUBLISHED_VERSION_ID)
                .build();
        WorkflowVersionEntity publishedVersion = WorkflowVersionEntity.builder()
                .id(PUBLISHED_VERSION_ID)
                .workflowId(WORKFLOW_ID)
                .versionState(WorkflowVersionState.PUBLISHED)
                .build();
        WorkflowStepEntity oldStep = WorkflowStepEntity.builder()
                .id(OLD_STEP_ID)
                .workflowVersionId(OLD_VERSION_ID)
                .statusId(CURRENT_STATUS_ID)
                .build();
        WorkflowStepEntity currentStep = WorkflowStepEntity.builder()
                .id(CURRENT_STEP_ID)
                .workflowVersionId(PUBLISHED_VERSION_ID)
                .statusId(CURRENT_STATUS_ID)
                .build();
        WorkflowStepEntity targetStep = WorkflowStepEntity.builder()
                .id(TARGET_STEP_ID)
                .workflowVersionId(PUBLISHED_VERSION_ID)
                .statusId(TARGET_STATUS_ID)
                .build();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .id(TRANSITION_ID)
                .workflowVersionId(PUBLISHED_VERSION_ID)
                .fromStepId(CURRENT_STEP_ID)
                .toStepId(TARGET_STEP_ID)
                .build();
        StatusEntity targetStatus = StatusEntity.builder()
                .id(TARGET_STATUS_ID)
                .categoryId(STATUS_CATEGORY_ID)
                .build();
        StatusCategoryEntity targetCategory = StatusCategoryEntity.builder()
                .id(STATUS_CATEGORY_ID)
                .build();

        when(issueTypeService.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(IssueTypeEntity.builder().build());
        when(workflowService.resolveWorkflow(WORKFLOW_SCHEME_ID, ISSUE_TYPE_ID, TENANT_ID)).thenReturn(workflow);
        when(workflowVersionPort.getWorkflowVersionById(PUBLISHED_VERSION_ID, TENANT_ID)).thenReturn(java.util.Optional.of(publishedVersion));
        when(workflowStepPort.getWorkflowStepById(OLD_STEP_ID, TENANT_ID)).thenReturn(java.util.Optional.of(oldStep));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(currentStep, targetStep));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(transition));
        when(workflowStepPort.getWorkflowStepById(TARGET_STEP_ID, TENANT_ID)).thenReturn(java.util.Optional.of(targetStep));
        when(statusService.getStatusById(TARGET_STATUS_ID, TENANT_ID)).thenReturn(targetStatus);
        when(statusService.getStatusCategoryByIdIncludingSystem(STATUS_CATEGORY_ID, TENANT_ID)).thenReturn(targetCategory);

        List<AvailableTransitionConfiguration> transitions = resolver.listAvailableTransitions(context, TENANT_ID);

        assertEquals(1, transitions.size());
        assertEquals(CURRENT_STEP_ID, transitions.getFirst().currentStep().getId());
        assertEquals(TARGET_STEP_ID, transitions.getFirst().targetStep().getId());
    }
}
