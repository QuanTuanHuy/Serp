/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.workitem.command.create.model.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.model.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.domain.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.enums.WorkflowVersionState;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.port.store.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
import serp.project.pmcore.domain.port.store.IWorkflowVersionPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemCreateConfigurationResolverTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ISSUE_TYPE_ID = 101L;
    private static final Long WORKFLOW_SCHEME_ID = 201L;
    private static final Long WORKFLOW_ID = 202L;
    private static final Long WORKFLOW_VERSION_ID = 203L;
    private static final Long WORKFLOW_STEP_ID = 204L;
    private static final Long PRIORITY_SCHEME_ID = 301L;
    private static final Long PRIORITY_ID = 302L;
    private static final Long ISSUE_SECURITY_SCHEME_ID = 401L;
    private static final Long SECURITY_LEVEL_ID = 402L;

    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IWorkflowSchemePort workflowSchemePort;
    @Mock
    private IWorkflowSchemeItemPort workflowSchemeItemPort;
    @Mock
    private IWorkflowPort workflowPort;
    @Mock
    private IWorkflowVersionPort workflowVersionPort;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IPrioritySchemePort prioritySchemePort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    @Mock
    private IIssueSecuritySchemePort issueSecuritySchemePort;
    @Mock
    private IIssueSecurityLevelPort issueSecurityLevelPort;

    private WorkItemCreateConfigurationResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new WorkItemCreateConfigurationResolver(
                issueTypePort,
                issueTypeSchemeItemPort,
                workflowSchemePort,
                workflowSchemeItemPort,
                workflowPort,
                workflowVersionPort,
                workflowStepPort,
                prioritySchemePort,
                prioritySchemeItemPort,
                issueSecuritySchemePort,
                issueSecurityLevelPort
        );
    }

    @Test
    void resolveShouldReturnIssueTypeWorkflowAndPriority() {
        ProjectEntity project = project();
        CreateWorkItemData request = CreateWorkItemData.builder()
                .issueTypeId(ISSUE_TYPE_ID)
                .summary("Create task")
                .build();

        stubIssueType();
        stubWorkflow();
        stubPriority();

        ResolvedWorkItemCreateConfiguration configuration = resolver.resolve(project, request, TENANT_ID);

        assertEquals(ISSUE_TYPE_ID, configuration.issueType().getId());
        assertEquals(WORKFLOW_STEP_ID, configuration.initialStep().getId());
        assertEquals(PRIORITY_ID, configuration.priorityId());
    }

    @Test
    void resolveSecurityLevelIdShouldApplyDefaultWhenOmitted() {
        when(issueSecuritySchemePort.getIssueSecuritySchemeById(ISSUE_SECURITY_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueSecuritySchemeEntity.builder()
                        .id(ISSUE_SECURITY_SCHEME_ID)
                        .defaultLevelId(SECURITY_LEVEL_ID)
                        .build()));

        Long securityLevelId = resolver.resolveSecurityLevelId(project(), null, TENANT_ID);

        assertEquals(SECURITY_LEVEL_ID, securityLevelId);
    }

    @Test
    void resolveSecurityLevelIdShouldRejectLevelOutsideScheme() {
        when(issueSecuritySchemePort.getIssueSecuritySchemeById(ISSUE_SECURITY_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueSecuritySchemeEntity.builder()
                        .id(ISSUE_SECURITY_SCHEME_ID)
                        .defaultLevelId(SECURITY_LEVEL_ID)
                        .build()));
        when(issueSecurityLevelPort.getIssueSecurityLevelsBySchemeId(ISSUE_SECURITY_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(IssueSecurityLevelEntity.builder().id(SECURITY_LEVEL_ID).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> resolver.resolveSecurityLevelId(project(), SECURITY_LEVEL_ID + 1, TENANT_ID)
        );

        assertEquals(DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME, exception.getErrorCode());
    }

    private ProjectEntity project() {
        return ProjectEntity.builder()
                .id(10L)
                .issueTypeSchemeId(11L)
                .workflowSchemeId(WORKFLOW_SCHEME_ID)
                .prioritySchemeId(PRIORITY_SCHEME_ID)
                .issueSecuritySchemeId(ISSUE_SECURITY_SCHEME_ID)
                .isArchived(false)
                .build();
    }

    private void stubIssueType() {
        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeEntity.builder()
                        .id(ISSUE_TYPE_ID)
                        .hierarchyLevel(1)
                        .build()));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(11L, TENANT_ID))
                .thenReturn(List.of(IssueTypeSchemeItemEntity.builder().issueTypeId(ISSUE_TYPE_ID).build()));
    }

    private void stubWorkflow() {
        when(workflowSchemePort.getWorkflowSchemeById(WORKFLOW_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(WorkflowSchemeEntity.builder()
                        .id(WORKFLOW_SCHEME_ID)
                        .defaultWorkflowId(WORKFLOW_ID)
                        .build()));
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(WORKFLOW_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(WorkflowSchemeItemEntity.builder()
                        .issueTypeId(ISSUE_TYPE_ID)
                        .workflowId(WORKFLOW_ID)
                        .build()));
        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID))
                .thenReturn(Optional.of(WorkflowEntity.builder()
                        .id(WORKFLOW_ID)
                        .currentPublishedVersionId(WORKFLOW_VERSION_ID)
                        .build()));
        when(workflowVersionPort.getWorkflowVersionById(WORKFLOW_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(WorkflowVersionEntity.builder()
                        .id(WORKFLOW_VERSION_ID)
                        .versionState(WorkflowVersionState.PUBLISHED)
                        .build()));
        when(workflowStepPort.getInitialStepByWorkflowVersionId(WORKFLOW_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(WorkflowStepEntity.builder()
                        .id(WORKFLOW_STEP_ID)
                        .statusId(999L)
                        .build()));
    }

    private void stubPriority() {
        when(prioritySchemePort.getPrioritySchemeById(PRIORITY_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(PrioritySchemeEntity.builder()
                        .id(PRIORITY_SCHEME_ID)
                        .defaultPriorityId(PRIORITY_ID)
                        .build()));
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(PRIORITY_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(PrioritySchemeItemEntity.builder().priorityId(PRIORITY_ID).build()));
    }
}
