/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service.impl;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
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
class PriorityServiceTest {

    private static final Long PRIORITY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IPriorityPort priorityPort;
    @Mock
    private IPrioritySchemePort prioritySchemePort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private PriorityService service;

    @BeforeEach
    void setUp() {
        service = new PriorityService(
                priorityPort,
                prioritySchemePort,
                prioritySchemeItemPort,
                workItemReadPort
        );
    }

    @Test
    void createPriorityShouldPersistTenantOwnedPriority() {
        PriorityEntity draft = PriorityEntity.builder()
                .name(" High Priority ")
                .description("  Important work  ")
                .iconUrl("https://serp.local/high.svg")
                .color("#FFAA00")
                .sequence(1)
                .build();

        when(priorityPort.existsByName(TENANT_ID, "High Priority")).thenReturn(false);
        when(priorityPort.getPriorityByPriorityKey(TENANT_ID, "high_priority")).thenReturn(Optional.empty());
        when(priorityPort.createPriority(any(PriorityEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriorityEntity created = service.createPriority(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<PriorityEntity> captor = ArgumentCaptor.forClass(PriorityEntity.class);
        verify(priorityPort).createPriority(captor.capture());
        PriorityEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("high_priority", persisted.getPriorityKey());
        assertEquals("High Priority", persisted.getName());
        assertEquals("Important work", persisted.getDescription());
        assertEquals("https://serp.local/high.svg", persisted.getIconUrl());
        assertEquals("#FFAA00", persisted.getColor());
        assertEquals(1, persisted.getSequence());
        assertFalse(persisted.isSystem());
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertEquals(USER_ID, persisted.getUpdatedBy());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertSame(persisted, created);
    }

    @Test
    void updatePriorityShouldApplyMutableFieldsAndAllowClearingOptionalFields() {
        PriorityEntity existing = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .description("Old")
                .iconUrl("https://serp.local/high-old.svg")
                .color("#AA0000")
                .sequence(1)
                .isSystem(false)
                .createdBy(USER_ID)
                .createdAt(100L)
                .build();

        when(priorityPort.getPriorityById(PRIORITY_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(priorityPort.existsByName(TENANT_ID, "Critical")).thenReturn(false);

        PriorityEntity updated = service.updatePriority(
                PRIORITY_ID,
                new PriorityUpdateData("Critical", true, null, true, null, true, null, true, 2, true),
                TENANT_ID,
                USER_ID
        );

        verify(priorityPort).updatePriority(existing);
        assertEquals("Critical", updated.getName());
        assertNull(updated.getDescription());
        assertNull(updated.getIconUrl());
        assertNull(updated.getColor());
        assertEquals(2, updated.getSequence());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void getVisiblePriorityByIdShouldReturnSystemOwnedPriority() {
        PriorityEntity systemPriority = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(0L)
                .priorityKey("highest")
                .name("Highest")
                .sequence(1)
                .isSystem(true)
                .build();

        when(priorityPort.getPriorityByIdIncludingSystem(PRIORITY_ID, TENANT_ID))
                .thenReturn(Optional.of(systemPriority));

        PriorityEntity visible = service.getVisiblePriorityById(PRIORITY_ID, TENANT_ID);

        assertSame(systemPriority, visible);
        assertTrue(visible.isSystem());
    }

    @Test
    void deletePriorityShouldRejectWhenPriorityIsStillInUse() {
        PriorityEntity existing = PriorityEntity.builder()
                .id(PRIORITY_ID)
                .tenantId(TENANT_ID)
                .priorityKey("high")
                .name("High")
                .sequence(1)
                .isSystem(false)
                .build();

        when(priorityPort.getPriorityById(PRIORITY_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workItemReadPort.getWorkItemsByPriorityId(PRIORITY_ID, TENANT_ID))
                .thenReturn(List.of(WorkItemEntity.builder().id(99L).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deletePriority(PRIORITY_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.PRIORITY_IN_USE, exception.getErrorCode());
        verify(priorityPort, never()).updatePriority(any(PriorityEntity.class));
    }
}
