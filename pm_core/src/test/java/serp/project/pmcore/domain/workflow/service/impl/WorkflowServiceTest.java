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
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    private static final Long WORKFLOW_ID = 10L;
    private static final Long DRAFT_VERSION_ID = 11L;
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

    private WorkflowService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowService(
                workflowSchemePort,
                workflowSchemeItemPort,
                workflowPort,
                workflowVersionPort,
                workflowStepPort
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
        assertNotEquals(Boolean.TRUE, persistedRoot.getIsSystem());
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

        assertEquals(Boolean.TRUE, result.getIsSystem());
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
}
