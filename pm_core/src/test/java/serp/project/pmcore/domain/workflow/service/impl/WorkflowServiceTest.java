/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workflow.validator.WorkflowDraftValidator;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long DRAFT_VERSION_ID = 11L;
    private static final Long STATUS_ID = 12L;
    private static final Long STEP_ID = 13L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

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
    private IWorkflowTransitionPort workflowTransitionPort;
    @Mock
    private IWorkflowTransitionRulePort workflowTransitionRulePort;
    @Mock
    private IScreenPort screenPort;
    @Mock
    private WorkflowDraftVersionCloner workflowDraftVersionCloner;
    @Mock
    private WorkflowDraftValidator workflowDraftValidator;
    @Mock
    private IStatusService statusService;

    private WorkflowService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowService(
                workflowSchemePort,
                workflowSchemeItemPort,
                workflowPort,
                workflowVersionPort,
                workflowStepPort,
                workflowTransitionPort,
                workflowTransitionRulePort,
                screenPort,
                workflowDraftVersionCloner,
                workflowDraftValidator,
                statusService
        );
    }

    @Test
    void createWorkflowShouldPersistDraftRootAndVersion() {
        when(workflowPort.getWorkflowByWorkflowKey(TENANT_ID, "team_workflow")).thenReturn(Optional.empty());
        when(workflowPort.createWorkflow(any(WorkflowEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowEntity entity = invocation.getArgument(0);
                    entity.setId(WORKFLOW_ID);
                    return entity;
                });
        when(workflowVersionPort.createWorkflowVersion(any(WorkflowVersionEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowVersionEntity entity = invocation.getArgument(0);
                    entity.setId(DRAFT_VERSION_ID);
                    return entity;
                });

        WorkflowEntity created = service.createWorkflow(
                WorkflowEntity.builder()
                        .name(" Team Workflow ")
                        .description(" Draft workflow ")
                        .build(),
                TENANT_ID,
                USER_ID
        );

        ArgumentCaptor<WorkflowEntity> workflowCaptor = ArgumentCaptor.forClass(WorkflowEntity.class);
        verify(workflowPort).createWorkflow(workflowCaptor.capture());
        WorkflowEntity persistedRoot = workflowCaptor.getValue();
        assertEquals(TENANT_ID, persistedRoot.getTenantId());
        assertEquals("team_workflow", persistedRoot.getWorkflowKey());
        assertEquals("Team Workflow", persistedRoot.getName());
        assertEquals("Draft workflow", persistedRoot.getDescription());
        assertEquals(WorkflowLifecycleState.INACTIVE, persistedRoot.getLifecycleState());
        assertFalse(Boolean.TRUE.equals(persistedRoot.getIsSystem()));
        assertNotNull(persistedRoot.getCreatedAt());

        ArgumentCaptor<WorkflowVersionEntity> versionCaptor = ArgumentCaptor.forClass(WorkflowVersionEntity.class);
        verify(workflowVersionPort).createWorkflowVersion(versionCaptor.capture());
        WorkflowVersionEntity draftVersion = versionCaptor.getValue();
        assertEquals(WORKFLOW_ID, draftVersion.getWorkflowId());
        assertEquals(1, draftVersion.getVersionNo());
        assertEquals(WorkflowVersionState.DRAFT, draftVersion.getVersionState());

        ArgumentCaptor<WorkflowEntity> updatedCaptor = ArgumentCaptor.forClass(WorkflowEntity.class);
        verify(workflowPort).updateWorkflow(updatedCaptor.capture());
        assertEquals(DRAFT_VERSION_ID, updatedCaptor.getValue().getDraftVersionId());

        assertEquals(DRAFT_VERSION_ID, created.getDraftVersionId());
        assertEquals(WORKFLOW_ID, created.getId());
    }

    @Test
    void createWorkflowShouldAppendUniqueKeySuffixWhenNeeded() {
        when(workflowPort.getWorkflowByWorkflowKey(TENANT_ID, "team_workflow"))
                .thenReturn(Optional.of(WorkflowEntity.builder().id(99L).tenantId(TENANT_ID).build()));
        when(workflowPort.getWorkflowByWorkflowKey(TENANT_ID, "team_workflow_2")).thenReturn(Optional.empty());
        when(workflowPort.createWorkflow(any(WorkflowEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowVersionPort.createWorkflowVersion(any(WorkflowVersionEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowVersionEntity entity = invocation.getArgument(0);
                    entity.setId(DRAFT_VERSION_ID);
                    return entity;
                });

        service.createWorkflow(
                WorkflowEntity.builder().name("Team Workflow").build(),
                TENANT_ID,
                USER_ID
        );

        ArgumentCaptor<WorkflowEntity> workflowCaptor = ArgumentCaptor.forClass(WorkflowEntity.class);
        verify(workflowPort).createWorkflow(workflowCaptor.capture());
        assertEquals("team_workflow_2", workflowCaptor.getValue().getWorkflowKey());
    }

    @Test
    void updateWorkflowShouldUpdateMetadataWhenDraftAlreadyExists() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .workflowKey("team_workflow")
                .name("Old Name")
                .description("Old Desc")
                .draftVersionId(DRAFT_VERSION_ID)
                .lifecycleState(WorkflowLifecycleState.INACTIVE)
                .build();
        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));

        WorkflowEntity updated = service.updateWorkflow(
                WORKFLOW_ID,
                " New Name ",
                " New Desc ",
                TENANT_ID,
                USER_ID
        );

        assertEquals("New Name", updated.getName());
        assertEquals("New Desc", updated.getDescription());
        assertEquals(DRAFT_VERSION_ID, updated.getDraftVersionId());
        verify(workflowDraftVersionCloner, never()).cloneVersionTree(any(), any(), any(), any());
        verify(workflowVersionPort, never()).createWorkflowVersion(any());
        verify(workflowPort).updateWorkflow(workflow);
    }

    @Test
    void updateWorkflowShouldCreateNewDraftFromPublishedVersionWhenActiveHasNoDraft() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .workflowKey("team_workflow")
                .name("Current Name")
                .description("Current Desc")
                .currentPublishedVersionId(90L)
                .draftVersionId(null)
                .lifecycleState(WorkflowLifecycleState.ACTIVE)
                .build();
        WorkflowVersionEntity publishedVersion = WorkflowVersionEntity.builder()
                .id(90L)
                .tenantId(TENANT_ID)
                .workflowId(WORKFLOW_ID)
                .versionNo(1)
                .versionState(WorkflowVersionState.PUBLISHED)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowVersionPort.getWorkflowVersionById(90L, TENANT_ID)).thenReturn(Optional.of(publishedVersion));
        when(workflowVersionPort.getWorkflowVersionsByWorkflowIdIncludingSystem(WORKFLOW_ID, TENANT_ID))
                .thenReturn(List.of(publishedVersion));
        when(workflowVersionPort.createWorkflowVersion(any(WorkflowVersionEntity.class)))
                .thenAnswer(invocation -> {
                    WorkflowVersionEntity entity = invocation.getArgument(0);
                    entity.setId(DRAFT_VERSION_ID);
                    return entity;
                });

        WorkflowEntity updated = service.updateWorkflow(
                WORKFLOW_ID,
                " Updated Name ",
                " Updated Desc ",
                TENANT_ID,
                USER_ID
        );

        ArgumentCaptor<WorkflowVersionEntity> versionCaptor = ArgumentCaptor.forClass(WorkflowVersionEntity.class);
        verify(workflowVersionPort).createWorkflowVersion(versionCaptor.capture());
        WorkflowVersionEntity createdDraft = versionCaptor.getValue();
        assertEquals(2, createdDraft.getVersionNo());
        assertEquals(WorkflowVersionState.DRAFT, createdDraft.getVersionState());
        assertEquals(90L, createdDraft.getBaseVersionId());

        verify(workflowDraftVersionCloner).cloneVersionTree(90L, DRAFT_VERSION_ID, TENANT_ID, USER_ID);
        assertEquals(DRAFT_VERSION_ID, updated.getDraftVersionId());
        assertEquals(90L, updated.getCurrentPublishedVersionId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated Desc", updated.getDescription());
    }

    @Test
    void getVisibleWorkflowByIdShouldReturnSystemWorkflow() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(0L)
                .workflowKey("software_kanban_workflow")
                .name("Software Kanban Workflow")
                .isSystem(true)
                .build();
        when(workflowPort.getWorkflowByIdIncludingSystem(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));

        WorkflowEntity result = service.getVisibleWorkflowById(WORKFLOW_ID, TENANT_ID);

        assertTrue(Boolean.TRUE.equals(result.getIsSystem()));
        assertEquals("software_kanban_workflow", result.getWorkflowKey());
    }

    @Test
    void listVisibleWorkflowsShouldDelegateToPort() {
        WorkflowListCriteria criteria = WorkflowListCriteria.builder()
                .search("kanban")
                .isActive(true)
                .isSystem(true)
                .build();
        when(workflowPort.listWorkflowsIncludingSystem(TENANT_ID, criteria))
                .thenReturn(new PageResult<>(List.of(WorkflowEntity.builder().id(WORKFLOW_ID).build()), 1L));

        PageResult<WorkflowEntity> result = service.listVisibleWorkflows(TENANT_ID, criteria);

        assertEquals(1L, result.total());
        verify(workflowPort).listWorkflowsIncludingSystem(TENANT_ID, criteria);
    }

    @Test
    void addWorkflowStepShouldAppendDraftStepFromVisibleStatus() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        StatusEntity status = StatusEntity.builder()
                .id(STATUS_ID)
                .tenantId(TENANT_ID)
                .statusKey("in_progress")
                .name("In Progress")
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID)).thenReturn(List.of());
        when(statusService.getVisibleStatusById(STATUS_ID, TENANT_ID)).thenReturn(status);
        when(workflowStepPort.createWorkflowSteps(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowStepEntity created = service.addWorkflowStep(WORKFLOW_ID, STATUS_ID, true, false, TENANT_ID, USER_ID);

        ArgumentCaptor<List<WorkflowStepEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowStepPort).createWorkflowSteps(captor.capture());
        WorkflowStepEntity persisted = captor.getValue().getFirst();
        assertEquals(DRAFT_VERSION_ID, persisted.getWorkflowVersionId());
        assertEquals("in_progress", persisted.getStepKey());
        assertEquals("In Progress", persisted.getName());
        assertEquals(1, persisted.getStepOrder());
        assertTrue(Boolean.TRUE.equals(created.getIsInitial()));
    }

    @Test
    void addWorkflowStepShouldRejectDuplicateStatusInDraft() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(WorkflowStepEntity.builder().id(STEP_ID).statusId(STATUS_ID).build()));
        when(statusService.getVisibleStatusById(STATUS_ID, TENANT_ID))
                .thenReturn(StatusEntity.builder().id(STATUS_ID).statusKey("in_progress").name("In Progress").build());

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.addWorkflowStep(WORKFLOW_ID, STATUS_ID, false, false, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.WORKFLOW_STEP_DUPLICATE_STATUS, exception.getErrorCode());
        verify(workflowStepPort, never()).createWorkflowSteps(any());
    }

    @Test
    void removeWorkflowStepShouldSoftDeleteStepAndRelatedTransitions() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowStepEntity step = WorkflowStepEntity.builder()
                .id(STEP_ID)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .id(40L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .fromStepId(STEP_ID)
                .toStepId(50L)
                .build();
        WorkflowTransitionRuleEntity rule = WorkflowTransitionRuleEntity.builder()
                .id(60L)
                .tenantId(TENANT_ID)
                .transitionId(40L)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepById(STEP_ID, TENANT_ID)).thenReturn(Optional.of(step));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(transition));
        when(workflowTransitionRulePort.getWorkflowTransitionRulesByTransitionId(40L, TENANT_ID))
                .thenReturn(List.of(rule));
        when(workflowTransitionRulePort.updateWorkflowTransitionRules(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowTransitionPort.updateWorkflowTransitions(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowStepPort.updateWorkflowSteps(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowStepEntity deleted = service.removeWorkflowStep(WORKFLOW_ID, STEP_ID, TENANT_ID, USER_ID);

        assertNotNull(deleted.getDeletedAt());
        verify(workflowTransitionRulePort).updateWorkflowTransitionRules(any());
        verify(workflowTransitionPort).updateWorkflowTransitions(any());
        verify(workflowStepPort).updateWorkflowSteps(any());
    }

    @Test
    void reorderWorkflowStepsShouldRewriteStepOrders() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowStepEntity first = WorkflowStepEntity.builder().id(100L).workflowVersionId(DRAFT_VERSION_ID).stepOrder(1).build();
        WorkflowStepEntity second = WorkflowStepEntity.builder().id(101L).workflowVersionId(DRAFT_VERSION_ID).stepOrder(2).build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(first, second));
        when(workflowStepPort.updateWorkflowSteps(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<WorkflowStepEntity> reordered = service.reorderWorkflowSteps(
                WORKFLOW_ID,
                List.of(101L, 100L),
                TENANT_ID,
                USER_ID
        );

        assertEquals(101L, reordered.get(0).getId());
        assertEquals(1, reordered.get(0).getStepOrder());
        assertEquals(100L, reordered.get(1).getId());
        assertEquals(2, reordered.get(1).getStepOrder());
    }

    @Test
    void addWorkflowTransitionShouldAllowGlobalTransitionAndAutoSequence() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowStepEntity targetStep = WorkflowStepEntity.builder()
                .id(101L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(targetStep));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(WorkflowTransitionEntity.builder().id(201L).sequence(1).build()));
        when(workflowTransitionPort.createWorkflowTransitions(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowTransitionEntity created = service.addWorkflowTransition(
                WORKFLOW_ID,
                "Approve",
                null,
                101L,
                null,
                null,
                TENANT_ID,
                USER_ID
        );

        ArgumentCaptor<List<WorkflowTransitionEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowTransitionPort).createWorkflowTransitions(captor.capture());
        WorkflowTransitionEntity persisted = captor.getValue().getFirst();
        assertEquals("Approve", persisted.getName());
        assertEquals(DRAFT_VERSION_ID, persisted.getWorkflowVersionId());
        assertEquals(101L, persisted.getToStepId());
        assertEquals(2, persisted.getSequence());
        assertEquals(null, persisted.getFromStepId());
        assertEquals(2, created.getSequence());
    }

    @Test
    void addWorkflowTransitionShouldRejectCrossVersionStepReference() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowStepEntity onlyDraftStep = WorkflowStepEntity.builder()
                .id(101L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(List.of(onlyDraftStep));

        var exception = assertThrows(
                serp.project.pmcore.domain.shared.exception.ResourceNotFoundException.class,
                () -> service.addWorkflowTransition(
                        WORKFLOW_ID,
                        "Approve",
                        999L,
                        101L,
                        null,
                        0,
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.WORKFLOW_STEP_NOT_FOUND, exception.getErrorCode());
        verify(workflowTransitionPort, never()).createWorkflowTransitions(any());
    }

    @Test
    void updateWorkflowTransitionShouldUpdateEditableDraftTransition() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .id(300L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .name("Old")
                .screenId(400L)
                .sequence(1)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowTransitionPort.getWorkflowTransitionByIdAndWorkflowVersionId(300L, DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(transition));
        when(screenPort.getScreenById(401L, TENANT_ID)).thenReturn(Optional.of(serp.project.pmcore.domain.screen.entity.ScreenEntity.builder().id(401L).build()));
        when(workflowTransitionPort.updateWorkflowTransitions(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowTransitionEntity updated = service.updateWorkflowTransition(
                WORKFLOW_ID,
                300L,
                "Ready for QA",
                401L,
                5,
                TENANT_ID,
                USER_ID
        );

        assertEquals("Ready for QA", updated.getName());
        assertEquals(401L, updated.getScreenId());
        assertEquals(5, updated.getSequence());
        verify(workflowTransitionPort).updateWorkflowTransitions(any());
    }

    @Test
    void removeWorkflowTransitionShouldSoftDeleteTransitionAndRules() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .id(400L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowTransitionRuleEntity rule = WorkflowTransitionRuleEntity.builder()
                .id(500L)
                .tenantId(TENANT_ID)
                .transitionId(400L)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowTransitionPort.getWorkflowTransitionByIdAndWorkflowVersionId(400L, DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(transition));
        when(workflowTransitionRulePort.getWorkflowTransitionRulesByTransitionId(400L, TENANT_ID))
                .thenReturn(List.of(rule));
        when(workflowTransitionRulePort.updateWorkflowTransitionRules(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workflowTransitionPort.updateWorkflowTransitions(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowTransitionEntity deleted = service.removeWorkflowTransition(WORKFLOW_ID, 400L, TENANT_ID, USER_ID);

        assertNotNull(deleted.getDeletedAt());
        verify(workflowTransitionRulePort).updateWorkflowTransitionRules(any());
        verify(workflowTransitionPort).updateWorkflowTransitions(any());
    }

    @Test
    void listWorkflowTransitionsShouldFilterByFromStepId() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .currentPublishedVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowStepEntity step = WorkflowStepEntity.builder()
                .id(600L)
                .tenantId(TENANT_ID)
                .workflowVersionId(DRAFT_VERSION_ID)
                .build();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .id(700L)
                .fromStepId(600L)
                .toStepId(601L)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowStepPort.getWorkflowStepsByWorkflowVersionId(DRAFT_VERSION_ID, TENANT_ID)).thenReturn(List.of(step));
        when(workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionIdAndFromStepId(DRAFT_VERSION_ID, 600L, TENANT_ID))
                .thenReturn(List.of(transition));

        List<WorkflowTransitionEntity> result = service.listWorkflowTransitions(WORKFLOW_ID, 600L, TENANT_ID);

        assertEquals(1, result.size());
        assertEquals(700L, result.getFirst().getId());
    }

    @Test
    void publishWorkflowShouldPromoteDraftAndArchiveCurrentPublishedVersion() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .currentPublishedVersionId(90L)
                .lifecycleState(WorkflowLifecycleState.INACTIVE)
                .build();
        WorkflowVersionEntity draftVersion = WorkflowVersionEntity.builder()
                .id(DRAFT_VERSION_ID)
                .tenantId(TENANT_ID)
                .workflowId(WORKFLOW_ID)
                .versionState(WorkflowVersionState.DRAFT)
                .build();
        WorkflowVersionEntity currentPublishedVersion = WorkflowVersionEntity.builder()
                .id(90L)
                .tenantId(TENANT_ID)
                .workflowId(WORKFLOW_ID)
                .versionState(WorkflowVersionState.PUBLISHED)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowDraftValidator.validateDraft(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(new WorkflowValidationResult(List.of(), List.of()));
        when(workflowVersionPort.getWorkflowVersionById(DRAFT_VERSION_ID, TENANT_ID)).thenReturn(Optional.of(draftVersion));
        when(workflowVersionPort.getWorkflowVersionById(90L, TENANT_ID)).thenReturn(Optional.of(currentPublishedVersion));

        WorkflowEntity published = service.publishWorkflow(WORKFLOW_ID, TENANT_ID, USER_ID);

        assertEquals(DRAFT_VERSION_ID, published.getCurrentPublishedVersionId());
        assertEquals(null, published.getDraftVersionId());
        assertEquals(WorkflowLifecycleState.ACTIVE, published.getLifecycleState());
        assertEquals(WorkflowVersionState.PUBLISHED, draftVersion.getVersionState());
        assertEquals(WorkflowVersionState.ARCHIVED, currentPublishedVersion.getVersionState());
        verify(workflowVersionPort).updateWorkflowVersion(currentPublishedVersion);
        verify(workflowVersionPort).updateWorkflowVersion(draftVersion);
        verify(workflowPort).updateWorkflow(workflow);
    }

    @Test
    void publishWorkflowShouldRejectInvalidDraft() {
        WorkflowEntity workflow = WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(TENANT_ID)
                .draftVersionId(DRAFT_VERSION_ID)
                .build();

        when(workflowPort.getWorkflowById(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow));
        when(workflowDraftValidator.validateDraft(DRAFT_VERSION_ID, TENANT_ID))
                .thenReturn(new WorkflowValidationResult(
                        List.of(new serp.project.pmcore.domain.workflow.dto.WorkflowValidationFinding(
                                "V-001",
                                serp.project.pmcore.domain.workflow.dto.WorkflowValidationSeverity.ERROR,
                                "Missing initial step"
                        )),
                        List.of()
                ));

        var exception = assertThrows(
                serp.project.pmcore.domain.shared.exception.DomainValidationException.class,
                () -> service.publishWorkflow(WORKFLOW_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.WORKFLOW_VALIDATION_FAILED, exception.getErrorCode());
        verify(workflowVersionPort, never()).updateWorkflowVersion(any());
        verify(workflowPort, never()).updateWorkflow(any());
    }
}
