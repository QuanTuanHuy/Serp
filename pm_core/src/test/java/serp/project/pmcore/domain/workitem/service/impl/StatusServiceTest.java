/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    private static final Long STATUS_ID = 10L;
    private static final Long CATEGORY_ID = 101L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IStatusPort statusPort;
    @Mock
    private IStatusCategoryPort statusCategoryPort;
    @Mock
    private IWorkflowStepPort workflowStepPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private StatusService service;

    @BeforeEach
    void setUp() {
        service = new StatusService(
                statusPort,
                statusCategoryPort,
                workflowStepPort,
                workItemReadPort
        );
    }

    @Test
    void createStatusShouldPersistTenantOwnedStatus() {
        StatusEntity draft = StatusEntity.builder()
                .statusKey(" in_progress ")
                .name(" In Progress ")
                .description(" Doing ")
                .iconUrl(" https://serp.local/in-progress.svg ")
                .categoryId(CATEGORY_ID)
                .build();

        when(statusPort.existsByStatusKey(TENANT_ID, "in_progress")).thenReturn(false);
        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(CATEGORY_ID, TENANT_ID))
                .thenReturn(Optional.of(StatusCategoryEntity.builder().id(CATEGORY_ID).build()));
        when(statusPort.createStatus(any(StatusEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StatusEntity created = service.createStatus(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<StatusEntity> captor = ArgumentCaptor.forClass(StatusEntity.class);
        verify(statusPort).createStatus(captor.capture());
        StatusEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("in_progress", persisted.getStatusKey());
        assertEquals("In Progress", persisted.getName());
        assertEquals("Doing", persisted.getDescription());
        assertEquals("https://serp.local/in-progress.svg", persisted.getIconUrl());
        assertEquals(CATEGORY_ID, persisted.getCategoryId());
        assertEquals(false, persisted.getIsSystem());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void getVisibleStatusByIdShouldReturnSystemStatus() {
        StatusEntity systemStatus = StatusEntity.builder()
                .id(STATUS_ID)
                .tenantId(0L)
                .statusKey("done")
                .name("Done")
                .isSystem(true)
                .build();

        when(statusPort.getStatusByIdIncludingSystem(STATUS_ID, TENANT_ID)).thenReturn(Optional.of(systemStatus));

        StatusEntity visible = service.getVisibleStatusById(STATUS_ID, TENANT_ID);

        assertSame(systemStatus, visible);
        assertEquals(true, visible.getIsSystem());
    }

    @Test
    void updateStatusShouldTreatSystemStatusAsNotFoundForWritePath() {
        when(statusPort.getStatusById(STATUS_ID, TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateStatus(
                        STATUS_ID,
                        new StatusUpdateData("done", true, null, false, null, false, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.STATUS_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void listVisibleStatusesShouldDelegateToPort() {
        StatusListCriteria criteria = StatusListCriteria.builder()
                .search("done")
                .isSystem(true)
                .build();
        PageResult<StatusEntity> expected = new PageResult<>(
                List.of(StatusEntity.builder().id(1L).name("Done").build()),
                1L
        );
        when(statusPort.listStatusesIncludingSystem(TENANT_ID, criteria)).thenReturn(expected);

        PageResult<StatusEntity> result = service.listVisibleStatuses(TENANT_ID, criteria);

        assertSame(expected, result);
    }

    @Test
    void deleteStatusShouldRejectWhenReferencedByWorkflowStep() {
        StatusEntity existing = StatusEntity.builder()
                .id(STATUS_ID)
                .tenantId(TENANT_ID)
                .statusKey("in_progress")
                .name("In Progress")
                .isSystem(false)
                .build();

        when(statusPort.getStatusById(STATUS_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workflowStepPort.existsByStatusIdIncludingSystem(STATUS_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteStatus(STATUS_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.STATUS_IN_USE_BY_WORKFLOW, exception.getErrorCode());
        verify(statusPort, never()).updateStatus(any(StatusEntity.class));
    }

    @Test
    void deleteStatusShouldRejectWhenReferencedByActiveWorkItems() {
        StatusEntity existing = StatusEntity.builder()
                .id(STATUS_ID)
                .tenantId(TENANT_ID)
                .statusKey("in_progress")
                .name("In Progress")
                .isSystem(false)
                .build();

        when(statusPort.getStatusById(STATUS_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workflowStepPort.existsByStatusIdIncludingSystem(STATUS_ID, TENANT_ID)).thenReturn(false);
        when(workItemReadPort.existsActiveWorkItemByStatusId(STATUS_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteStatus(STATUS_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.STATUS_IN_USE_BY_WORK_ITEMS, exception.getErrorCode());
        verify(statusPort, never()).updateStatus(any(StatusEntity.class));
    }
}
