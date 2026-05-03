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
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.dto.WorkflowSchemeUpdateData;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowSchemeServiceTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 11L;
    private static final Long WORKFLOW_ID = 12L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;
    private static final Long PROJECT_ID = 40L;
    private static final Long ISSUE_TYPE_SCHEME_ID = 50L;
    private static final Long PUBLISHED_VERSION_ID = 60L;

    @Mock
    private IWorkflowSchemePort workflowSchemePort;
    @Mock
    private IWorkflowSchemeItemPort workflowSchemeItemPort;
    @Mock
    private IWorkflowPort workflowPort;
    @Mock
    private IWorkflowVersionPort workflowVersionPort;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IIssueTypeSchemePort issueTypeSchemePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IProjectReadPort projectReadPort;

    private WorkflowSchemeService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowSchemeService(
                workflowSchemePort,
                workflowSchemeItemPort,
                workflowPort,
                workflowVersionPort,
                issueTypePort,
                issueTypeSchemePort,
                issueTypeSchemeItemPort,
                projectReadPort
        );
    }

    @Test
    void createWorkflowSchemeShouldPersistTenantOwnedScheme() {
        WorkflowSchemeEntity draft = WorkflowSchemeEntity.builder()
                .name(" Team Managed ")
                .description("  Team scheme  ")
                .defaultWorkflowId(WORKFLOW_ID)
                .build();

        when(workflowSchemePort.existsByName(TENANT_ID, "Team Managed")).thenReturn(false);
        when(workflowPort.getWorkflowByIdIncludingSystem(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow(false)));
        when(workflowVersionPort.getWorkflowVersionByIdIncludingSystem(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(publishedVersion()));
        when(workflowSchemePort.createWorkflowScheme(any(WorkflowSchemeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkflowSchemeEntity created = service.createWorkflowScheme(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<WorkflowSchemeEntity> captor = ArgumentCaptor.forClass(WorkflowSchemeEntity.class);
        verify(workflowSchemePort).createWorkflowScheme(captor.capture());
        WorkflowSchemeEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Team Managed", persisted.getName());
        assertEquals("Team scheme", persisted.getDescription());
        assertEquals(WORKFLOW_ID, persisted.getDefaultWorkflowId());
        assertFalse(persisted.isSystem());
        assertNotNull(persisted.getCreatedAt());
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertSame(persisted, created);
    }

    @Test
    void getVisibleWorkflowSchemeDetailByIdShouldReturnSystemSchemeWithItems() {
        WorkflowSchemeEntity systemScheme = WorkflowSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(0L)
                .name("System Scheme")
                .defaultWorkflowId(WORKFLOW_ID)
                .build();
        WorkflowSchemeItemEntity item = WorkflowSchemeItemEntity.builder()
                .id(100L)
                .tenantId(0L)
                .schemeId(SCHEME_ID)
                .issueTypeId(ISSUE_TYPE_ID)
                .workflowId(WORKFLOW_ID)
                .build();

        when(workflowSchemePort.getWorkflowSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(systemScheme));
        when(workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(item));

        WorkflowSchemeEntity detail = service.getVisibleWorkflowSchemeDetailById(SCHEME_ID, TENANT_ID);

        assertTrue(detail.isSystem());
        assertEquals(1, detail.getItems().size());
        assertEquals(ISSUE_TYPE_ID, detail.getItems().getFirst().getIssueTypeId());
    }

    @Test
    void updateWorkflowSchemeShouldTreatSystemSchemeAsNotFoundForWritePath() {
        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateWorkflowScheme(
                        SCHEME_ID,
                        new WorkflowSchemeUpdateData("Renamed", true, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateWorkflowSchemeShouldRejectInactiveDefaultWorkflow() {
        WorkflowSchemeEntity existing = scheme(false);

        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workflowPort.getWorkflowByIdIncludingSystem(99L, TENANT_ID))
                .thenReturn(Optional.of(WorkflowEntity.builder()
                        .id(99L)
                        .tenantId(TENANT_ID)
                        .currentPublishedVersionId(null)
                        .lifecycleState(WorkflowLifecycleState.INACTIVE)
                        .build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.updateWorkflowScheme(
                        SCHEME_ID,
                        new WorkflowSchemeUpdateData(null, false, null, false, 99L, true),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.WORKFLOW_NOT_ACTIVE, exception.getErrorCode());
    }

    @Test
    void deleteWorkflowSchemeShouldRejectWhenBoundToActiveProjects() {
        WorkflowSchemeEntity existing = scheme(false);

        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectReadPort.existsActiveProjectByWorkflowSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteWorkflowScheme(SCHEME_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.WORKFLOW_SCHEME_BOUND_TO_PROJECT, exception.getErrorCode());
        verify(workflowSchemePort, never()).updateWorkflowScheme(any(WorkflowSchemeEntity.class));
    }

    @Test
    void replaceWorkflowSchemeItemsShouldRejectDuplicateIssueTypeIds() {
        WorkflowSchemeEntity existing = scheme(false);
        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.replaceWorkflowSchemeItems(
                        SCHEME_ID,
                        List.of(
                                new serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService.WorkflowSchemeItemReplacement(ISSUE_TYPE_ID, WORKFLOW_ID),
                                new serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService.WorkflowSchemeItemReplacement(ISSUE_TYPE_ID, 99L)
                        ),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals("items must not contain duplicate issueTypeId values", exception.getMessage());
    }

    @Test
    void replaceWorkflowSchemeItemsShouldPersistReplacement() {
        WorkflowSchemeEntity existing = scheme(false);
        WorkflowSchemeItemEntity saved = WorkflowSchemeItemEntity.builder()
                .id(1L)
                .schemeId(SCHEME_ID)
                .issueTypeId(ISSUE_TYPE_ID)
                .workflowId(WORKFLOW_ID)
                .build();

        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(issueTypePort.getIssueTypesByIdsIncludingSystem(List.of(ISSUE_TYPE_ID), TENANT_ID))
                .thenReturn(List.of(IssueTypeEntity.builder().id(ISSUE_TYPE_ID).tenantId(TENANT_ID).build()));
        when(workflowPort.getWorkflowByIdIncludingSystem(WORKFLOW_ID, TENANT_ID)).thenReturn(Optional.of(workflow(false)));
        when(workflowVersionPort.getWorkflowVersionByIdIncludingSystem(PUBLISHED_VERSION_ID, TENANT_ID))
                .thenReturn(Optional.of(publishedVersion()));
        when(projectReadPort.getActiveProjectIdsByWorkflowSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of());
        when(workflowSchemeItemPort.createWorkflowSchemeItems(any())).thenReturn(List.of(saved));

        WorkflowSchemeEntity updated = service.replaceWorkflowSchemeItems(
                SCHEME_ID,
                List.of(new serp.project.pmcore.domain.workflow.service.IWorkflowSchemeService.WorkflowSchemeItemReplacement(
                        ISSUE_TYPE_ID,
                        WORKFLOW_ID
                )),
                TENANT_ID,
                USER_ID
        );

        verify(workflowSchemeItemPort).deleteWorkflowSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID);
        verify(workflowSchemePort).updateWorkflowScheme(existing);
        assertEquals(1, updated.getItems().size());
        assertEquals(ISSUE_TYPE_ID, updated.getItems().getFirst().getIssueTypeId());
        assertEquals(USER_ID, updated.getUpdatedBy());
    }

    @Test
    void updateWorkflowSchemeShouldRejectCoverageGapForBoundProject() {
        WorkflowSchemeEntity existing = scheme(false);
        existing.setDefaultWorkflowId(null);
        existing.setItems(List.of());

        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .issueTypeSchemeId(ISSUE_TYPE_SCHEME_ID)
                .workflowSchemeId(SCHEME_ID)
                .build();
        IssueTypeSchemeEntity issueTypeScheme = IssueTypeSchemeEntity.builder()
                .id(ISSUE_TYPE_SCHEME_ID)
                .tenantId(TENANT_ID)
                .defaultIssueTypeId(null)
                .build();

        when(workflowSchemePort.getWorkflowSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectReadPort.getActiveProjectIdsByWorkflowSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of(PROJECT_ID));
        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(project));
        when(issueTypeSchemePort.getIssueTypeSchemeById(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(issueTypeScheme));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(ISSUE_TYPE_SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(IssueTypeSchemeItemEntity.builder().issueTypeId(ISSUE_TYPE_ID).build()));

        DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> service.updateWorkflowScheme(
                        SCHEME_ID,
                        new WorkflowSchemeUpdateData("Renamed", true, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING, exception.getErrorCode());
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

    private WorkflowEntity workflow(boolean system) {
        return WorkflowEntity.builder()
                .id(WORKFLOW_ID)
                .tenantId(system ? 0L : TENANT_ID)
                .workflowKey("default_workflow")
                .name("Default Workflow")
                .currentPublishedVersionId(PUBLISHED_VERSION_ID)
                .lifecycleState(WorkflowLifecycleState.ACTIVE)
                .isSystem(system)
                .build();
    }

    private WorkflowVersionEntity publishedVersion() {
        return WorkflowVersionEntity.builder()
                .id(PUBLISHED_VERSION_ID)
                .tenantId(TENANT_ID)
                .workflowId(WORKFLOW_ID)
                .versionNo(1)
                .versionState(WorkflowVersionState.PUBLISHED)
                .build();
    }
}
