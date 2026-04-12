/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeScreenSchemeItemPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueTypeServiceTest {

    private static final Long ISSUE_TYPE_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IIssueTypeSchemePort issueTypeSchemePort;
    @Mock
    private IIssueTypeScreenSchemeItemPort issueTypeScreenSchemeItemPort;
    @Mock
    private IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;
    @Mock
    private IWorkflowSchemeItemPort workflowSchemeItemPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private IssueTypeService service;

    @BeforeEach
    void setUp() {
        service = new IssueTypeService(
                issueTypePort,
                issueTypeSchemeItemPort,
                issueTypeSchemePort,
                issueTypeScreenSchemeItemPort,
                fieldConfigSchemeItemPort,
                workflowSchemeItemPort,
                workItemReadPort
        );
    }

    @Test
    void createIssueTypeShouldPersistTenantOwnedIssueType() {
        IssueTypeEntity draft = IssueTypeEntity.builder()
                .typeKey(" task ")
                .name(" Task ")
                .description("  Work item  ")
                .iconUrl("https://serp.local/task.svg")
                .hierarchyLevel(1)
                .build();

        when(issueTypePort.existsByTypeKey(TENANT_ID, "task")).thenReturn(false);
        when(issueTypePort.createIssueType(any(IssueTypeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IssueTypeEntity created = service.createIssueType(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<IssueTypeEntity> captor = ArgumentCaptor.forClass(IssueTypeEntity.class);
        verify(issueTypePort).createIssueType(captor.capture());
        IssueTypeEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("task", persisted.getTypeKey());
        assertEquals("Task", persisted.getName());
        assertEquals("Work item", persisted.getDescription());
        assertEquals("https://serp.local/task.svg", persisted.getIconUrl());
        assertEquals(1, persisted.getHierarchyLevel());
        assertFalse(persisted.isSystem());
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertEquals(USER_ID, persisted.getUpdatedBy());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertSame(persisted, created);
    }

    @Test
    void updateIssueTypeShouldApplyMutableFieldsAndAllowClearingOptionalFields() {
        IssueTypeEntity existing = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .description("Old")
                .iconUrl("https://serp.local/task-old.svg")
                .hierarchyLevel(1)
                .isSystem(false)
                .createdBy(USER_ID)
                .createdAt(100L)
                .build();

        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(Optional.of(existing));

        IssueTypeEntity updated = service.updateIssueType(
                ISSUE_TYPE_ID,
                new IssueTypeUpdateData("Updated Task", true, null, true, null, true, 2, true),
                TENANT_ID,
                USER_ID
        );

        verify(issueTypePort).updateIssueType(existing);
        assertEquals("Updated Task", updated.getName());
        assertNull(updated.getDescription());
        assertNull(updated.getIconUrl());
        assertEquals(2, updated.getHierarchyLevel());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void getVisibleIssueTypeByIdShouldReturnSystemOwnedIssueType() {
        IssueTypeEntity systemIssueType = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(0L)
                .typeKey("bug")
                .name("Bug")
                .hierarchyLevel(1)
                .isSystem(true)
                .build();

        when(issueTypePort.getIssueTypeByIdIncludingSystem(ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(Optional.of(systemIssueType));

        IssueTypeEntity visible = service.getVisibleIssueTypeById(ISSUE_TYPE_ID, TENANT_ID);

        assertSame(systemIssueType, visible);
        assertTrue(visible.isSystem());
    }

    @Test
    void deleteIssueTypeShouldRejectWhenIssueTypeIsStillInUse() {
        IssueTypeEntity existing = IssueTypeEntity.builder()
                .id(ISSUE_TYPE_ID)
                .tenantId(TENANT_ID)
                .typeKey("task")
                .name("Task")
                .hierarchyLevel(1)
                .isSystem(false)
                .build();

        when(issueTypePort.getIssueTypeById(ISSUE_TYPE_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workItemReadPort.getWorkItemsByIssueTypeId(ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(List.of(WorkItemEntity.builder().id(99L).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteIssueType(ISSUE_TYPE_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.ISSUE_TYPE_IN_USE, exception.getErrorCode());
        verify(issueTypePort, never()).updateIssueType(any(IssueTypeEntity.class));
    }
}
