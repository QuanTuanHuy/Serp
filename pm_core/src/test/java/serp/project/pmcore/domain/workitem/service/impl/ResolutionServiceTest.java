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
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;
import serp.project.pmcore.domain.workitem.entity.ResolutionEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Optional;

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
class ResolutionServiceTest {

    private static final Long RESOLUTION_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IResolutionPort resolutionPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private ResolutionService service;

    @BeforeEach
    void setUp() {
        service = new ResolutionService(resolutionPort, workItemReadPort);
    }

    @Test
    void createResolutionShouldPersistTenantOwnedResolution() {
        ResolutionEntity draft = ResolutionEntity.builder()
                .name(" Done ")
                .description("  Completed work  ")
                .sequence(1)
                .build();

        when(resolutionPort.getResolutionByNameIncludingSystem(TENANT_ID, "Done")).thenReturn(Optional.empty());
        when(resolutionPort.createResolution(any(ResolutionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResolutionEntity created = service.createResolution(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<ResolutionEntity> captor = ArgumentCaptor.forClass(ResolutionEntity.class);
        verify(resolutionPort).createResolution(captor.capture());
        ResolutionEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Done", persisted.getName());
        assertEquals("Completed work", persisted.getDescription());
        assertEquals(1, persisted.getSequence());
        assertFalse(Boolean.TRUE.equals(persisted.getIsSystem()));
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertEquals(USER_ID, persisted.getUpdatedBy());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
        assertSame(persisted, created);
    }

    @Test
    void createResolutionShouldRejectNameDuplicatedWithSystemResolution() {
        when(resolutionPort.getResolutionByNameIncludingSystem(TENANT_ID, "Done"))
                .thenReturn(Optional.of(ResolutionEntity.builder().id(99L).tenantId(0L).name("Done").build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.createResolution(ResolutionEntity.builder().name("Done").sequence(1).build(), TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.CONFLICT, exception.getErrorCode());
        verify(resolutionPort, never()).createResolution(any());
    }

    @Test
    void updateResolutionShouldApplyMutableFieldsAndAllowClearingDescription() {
        ResolutionEntity existing = ResolutionEntity.builder()
                .id(RESOLUTION_ID)
                .tenantId(TENANT_ID)
                .name("Done")
                .description("Old")
                .sequence(1)
                .isSystem(false)
                .createdBy(USER_ID)
                .createdAt(100L)
                .build();

        when(resolutionPort.getResolutionById(RESOLUTION_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(resolutionPort.getResolutionByNameIncludingSystem(TENANT_ID, "Fixed")).thenReturn(Optional.empty());

        ResolutionEntity updated = service.updateResolution(
                RESOLUTION_ID,
                new ResolutionUpdateData("Fixed", true, null, true, 2, true),
                TENANT_ID,
                USER_ID
        );

        verify(resolutionPort).updateResolution(existing);
        assertEquals("Fixed", updated.getName());
        assertNull(updated.getDescription());
        assertEquals(2, updated.getSequence());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void getVisibleResolutionByIdShouldReturnSystemOwnedResolution() {
        ResolutionEntity systemResolution = ResolutionEntity.builder()
                .id(RESOLUTION_ID)
                .tenantId(0L)
                .name("Done")
                .sequence(1)
                .isSystem(true)
                .build();

        when(resolutionPort.getResolutionByIdIncludingSystem(RESOLUTION_ID, TENANT_ID))
                .thenReturn(Optional.of(systemResolution));

        ResolutionEntity visible = service.getVisibleResolutionById(RESOLUTION_ID, TENANT_ID);

        assertSame(systemResolution, visible);
        assertTrue(Boolean.TRUE.equals(visible.getIsSystem()));
    }

    @Test
    void deleteResolutionShouldRejectWhenResolutionIsStillInUse() {
        ResolutionEntity existing = ResolutionEntity.builder()
                .id(RESOLUTION_ID)
                .tenantId(TENANT_ID)
                .name("Done")
                .sequence(1)
                .isSystem(false)
                .build();

        when(resolutionPort.getResolutionById(RESOLUTION_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(workItemReadPort.getWorkItemsByResolutionId(RESOLUTION_ID, TENANT_ID))
                .thenReturn(List.of(WorkItemEntity.builder().id(99L).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteResolution(RESOLUTION_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.RESOLUTION_IN_USE, exception.getErrorCode());
        verify(resolutionPort, never()).updateResolution(any(ResolutionEntity.class));
    }
}
