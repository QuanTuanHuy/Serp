/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.priority.port.IPrioritySchemePort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

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
class PrioritySchemeServiceTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long PRIORITY_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IPrioritySchemePort prioritySchemePort;
    @Mock
    private IPrioritySchemeItemPort prioritySchemeItemPort;
    @Mock
    private IPriorityPort priorityPort;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private PrioritySchemeService service;

    @BeforeEach
    void setUp() {
        service = new PrioritySchemeService(
                prioritySchemePort,
                prioritySchemeItemPort,
                priorityPort,
                projectReadPort,
                workItemReadPort
        );
    }

    @Test
    void createPrioritySchemeShouldPersistTenantOwnedScheme() {
        PrioritySchemeEntity draft = PrioritySchemeEntity.builder()
                .name(" Team Managed ")
                .description("  Team scheme  ")
                .defaultPriorityId(PRIORITY_ID)
                .build();

        when(prioritySchemePort.existsByName(TENANT_ID, "Team Managed")).thenReturn(false);
        when(priorityPort.getPriorityByIdIncludingSystem(PRIORITY_ID, TENANT_ID))
                .thenReturn(Optional.of(PriorityEntity.builder().id(PRIORITY_ID).tenantId(TENANT_ID).build()));
        when(prioritySchemePort.createPriorityScheme(any(PrioritySchemeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PrioritySchemeEntity created = service.createPriorityScheme(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<PrioritySchemeEntity> captor = ArgumentCaptor.forClass(PrioritySchemeEntity.class);
        verify(prioritySchemePort).createPriorityScheme(captor.capture());
        PrioritySchemeEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Team Managed", persisted.getName());
        assertEquals("Team scheme", persisted.getDescription());
        assertEquals(PRIORITY_ID, persisted.getDefaultPriorityId());
        assertFalse(persisted.isSystem());
        assertNotNull(persisted.getCreatedAt());
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertSame(persisted, created);
    }

    @Test
    void getVisiblePrioritySchemeDetailByIdShouldReturnSystemSchemeWithItems() {
        PrioritySchemeEntity systemScheme = PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(0L)
                .name("System Scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();
        PrioritySchemeItemEntity item = PrioritySchemeItemEntity.builder()
                .id(100L)
                .tenantId(0L)
                .schemeId(SCHEME_ID)
                .priorityId(PRIORITY_ID)
                .sequence(1)
                .build();

        when(prioritySchemePort.getPrioritySchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(systemScheme));
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(item));

        PrioritySchemeEntity detail = service.getVisiblePrioritySchemeDetailById(SCHEME_ID, TENANT_ID);

        assertTrue(detail.isSystem());
        assertEquals(1, detail.getItems().size());
        assertEquals(PRIORITY_ID, detail.getItems().getFirst().getPriorityId());
    }

    @Test
    void updatePrioritySchemeShouldTreatSystemSchemeAsNotFoundForWritePath() {
        when(prioritySchemePort.getPrioritySchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updatePriorityScheme(
                        SCHEME_ID,
                        new PrioritySchemeUpdateData("Renamed", true, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updatePrioritySchemeShouldRejectDefaultOutsideCurrentItems() {
        PrioritySchemeEntity existing = PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();

        when(prioritySchemePort.getPrioritySchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(priorityPort.getPriorityByIdIncludingSystem(99L, TENANT_ID))
                .thenReturn(Optional.of(PriorityEntity.builder().id(99L).tenantId(TENANT_ID).build()));
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(PrioritySchemeItemEntity.builder().priorityId(PRIORITY_ID).sequence(1).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.updatePriorityScheme(
                        SCHEME_ID,
                        new PrioritySchemeUpdateData(null, false, null, false, 99L, true),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS, exception.getErrorCode());
    }

    @Test
    void deletePrioritySchemeShouldRejectWhenBoundToActiveProjects() {
        PrioritySchemeEntity existing = PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();

        when(prioritySchemePort.getPrioritySchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectReadPort.existsActiveProjectByPrioritySchemeId(SCHEME_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deletePriorityScheme(SCHEME_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.PRIORITY_SCHEME_BOUND_TO_PROJECT, exception.getErrorCode());
        verify(prioritySchemePort, never()).updatePriorityScheme(any(PrioritySchemeEntity.class));
    }

    @Test
    void replacePrioritySchemeItemsShouldRejectWhenRemovedPriorityStillHasWorkItems() {
        PrioritySchemeEntity existing = PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();

        when(prioritySchemePort.getPrioritySchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(priorityPort.getPrioritiesByIdsIncludingSystem(List.of(PRIORITY_ID), TENANT_ID))
                .thenReturn(List.of(PriorityEntity.builder().id(PRIORITY_ID).tenantId(TENANT_ID).build()));
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(
                        PrioritySchemeItemEntity.builder().priorityId(PRIORITY_ID).sequence(1).build(),
                        PrioritySchemeItemEntity.builder().priorityId(99L).sequence(2).build()
                ));
        when(projectReadPort.getActiveProjectIdsByPrioritySchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of(1000L));
        when(workItemReadPort.getActivePriorityIdsInUseByProjectIds(TENANT_ID, List.of(1000L), List.of(99L)))
                .thenReturn(List.of(99L));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.replacePrioritySchemeItems(SCHEME_ID, List.of(PRIORITY_ID), TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.PRIORITY_SCHEME_IN_USE, exception.getErrorCode());
        verify(prioritySchemeItemPort, never()).deletePrioritySchemeItemsBySchemeId(SCHEME_ID, TENANT_ID);
    }

    @Test
    void replacePrioritySchemeItemsShouldPersistOrderedReplacement() {
        PrioritySchemeEntity existing = PrioritySchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultPriorityId(PRIORITY_ID)
                .build();
        PrioritySchemeItemEntity first = PrioritySchemeItemEntity.builder().id(1L).priorityId(PRIORITY_ID).sequence(1).build();
        PrioritySchemeItemEntity second = PrioritySchemeItemEntity.builder().id(2L).priorityId(12L).sequence(2).build();

        when(prioritySchemePort.getPrioritySchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(priorityPort.getPrioritiesByIdsIncludingSystem(List.of(PRIORITY_ID, 12L), TENANT_ID))
                .thenReturn(List.of(
                        PriorityEntity.builder().id(PRIORITY_ID).tenantId(TENANT_ID).build(),
                        PriorityEntity.builder().id(12L).tenantId(TENANT_ID).build()
                ));
        when(prioritySchemeItemPort.getPrioritySchemeItemsBySchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of());
        when(prioritySchemeItemPort.createPrioritySchemeItems(any())).thenReturn(List.of(first, second));

        PrioritySchemeEntity updated = service.replacePrioritySchemeItems(
                SCHEME_ID,
                List.of(PRIORITY_ID, 12L),
                TENANT_ID,
                USER_ID
        );

        verify(prioritySchemeItemPort).deletePrioritySchemeItemsBySchemeId(SCHEME_ID, TENANT_ID);
        verify(prioritySchemePort).updatePriorityScheme(existing);
        assertEquals(2, updated.getItems().size());
        assertEquals(PRIORITY_ID, updated.getItems().getFirst().getPriorityId());
        assertEquals(2, updated.getItems().get(1).getSequence());
        assertEquals(USER_ID, updated.getUpdatedBy());
    }
}
