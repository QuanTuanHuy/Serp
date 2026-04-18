/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuetype.dto.IssueTypeSchemeUpdateData;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;

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
class IssueTypeSchemeServiceTest {

    private static final Long SCHEME_ID = 10L;
    private static final Long ISSUE_TYPE_ID = 11L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IIssueTypeSchemePort issueTypeSchemePort;
    @Mock
    private IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    @Mock
    private IIssueTypePort issueTypePort;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IWorkItemReadPort workItemReadPort;

    private IssueTypeSchemeService service;

    @BeforeEach
    void setUp() {
        service = new IssueTypeSchemeService(
                issueTypeSchemePort,
                issueTypeSchemeItemPort,
                issueTypePort,
                projectReadPort,
                workItemReadPort
        );
    }

    @Test
    void createIssueTypeSchemeShouldPersistTenantOwnedScheme() {
        IssueTypeSchemeEntity draft = IssueTypeSchemeEntity.builder()
                .name(" Team Managed ")
                .description("  Team scheme  ")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();

        when(issueTypeSchemePort.existsByName(TENANT_ID, "Team Managed")).thenReturn(false);
        when(issueTypePort.getIssueTypeByIdIncludingSystem(ISSUE_TYPE_ID, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeEntity.builder().id(ISSUE_TYPE_ID).tenantId(TENANT_ID).build()));
        when(issueTypeSchemePort.createIssueTypeScheme(any(IssueTypeSchemeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IssueTypeSchemeEntity created = service.createIssueTypeScheme(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<IssueTypeSchemeEntity> captor = ArgumentCaptor.forClass(IssueTypeSchemeEntity.class);
        verify(issueTypeSchemePort).createIssueTypeScheme(captor.capture());
        IssueTypeSchemeEntity persisted = captor.getValue();

        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Team Managed", persisted.getName());
        assertEquals("Team scheme", persisted.getDescription());
        assertEquals(ISSUE_TYPE_ID, persisted.getDefaultIssueTypeId());
        assertFalse(persisted.isSystem());
        assertNotNull(persisted.getCreatedAt());
        assertEquals(USER_ID, persisted.getCreatedBy());
        assertSame(persisted, created);
    }

    @Test
    void getVisibleIssueTypeSchemeDetailByIdShouldReturnSystemSchemeWithItems() {
        IssueTypeSchemeEntity systemScheme = IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(0L)
                .name("System Scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();
        IssueTypeSchemeItemEntity item = IssueTypeSchemeItemEntity.builder()
                .id(100L)
                .tenantId(0L)
                .schemeId(SCHEME_ID)
                .issueTypeId(ISSUE_TYPE_ID)
                .sequence(1)
                .build();

        when(issueTypeSchemePort.getIssueTypeSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(systemScheme));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(item));

        IssueTypeSchemeEntity detail = service.getVisibleIssueTypeSchemeDetailById(SCHEME_ID, TENANT_ID);

        assertTrue(detail.isSystem());
        assertEquals(1, detail.getItems().size());
        assertEquals(ISSUE_TYPE_ID, detail.getItems().getFirst().getIssueTypeId());
    }

    @Test
    void updateIssueTypeSchemeShouldTreatSystemSchemeAsNotFoundForWritePath() {
        when(issueTypeSchemePort.getIssueTypeSchemeById(SCHEME_ID, TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateIssueTypeScheme(
                        SCHEME_ID,
                        new IssueTypeSchemeUpdateData("Renamed", true, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void updateIssueTypeSchemeShouldRejectDefaultOutsideCurrentItems() {
        IssueTypeSchemeEntity existing = IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();

        when(issueTypeSchemePort.getIssueTypeSchemeById(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(existing));
        when(issueTypePort.getIssueTypeByIdIncludingSystem(99L, TENANT_ID))
                .thenReturn(Optional.of(IssueTypeEntity.builder().id(99L).tenantId(TENANT_ID).build()));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(
                        IssueTypeSchemeItemEntity.builder().issueTypeId(ISSUE_TYPE_ID).sequence(1).build()
                ));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.updateIssueTypeScheme(
                        SCHEME_ID,
                        new IssueTypeSchemeUpdateData(null, false, null, false, 99L, true),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS, exception.getErrorCode());
    }

    @Test
    void deleteIssueTypeSchemeShouldRejectWhenBoundToActiveProjects() {
        IssueTypeSchemeEntity existing = IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();

        when(issueTypeSchemePort.getIssueTypeSchemeById(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(existing));
        when(projectReadPort.existsActiveProjectByIssueTypeSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteIssueTypeScheme(SCHEME_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.ISSUE_TYPE_SCHEME_BOUND_TO_PROJECT, exception.getErrorCode());
        verify(issueTypeSchemePort, never()).updateIssueTypeScheme(any(IssueTypeSchemeEntity.class));
    }

    @Test
    void replaceIssueTypeSchemeItemsShouldRejectWhenRemovedIssueTypeStillHasWorkItems() {
        IssueTypeSchemeEntity existing = IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();

        when(issueTypeSchemePort.getIssueTypeSchemeById(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(existing));
        when(issueTypePort.getIssueTypesByIdsIncludingSystem(List.of(ISSUE_TYPE_ID), TENANT_ID))
                .thenReturn(List.of(IssueTypeEntity.builder().id(ISSUE_TYPE_ID).tenantId(TENANT_ID).build()));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(
                        IssueTypeSchemeItemEntity.builder().issueTypeId(ISSUE_TYPE_ID).sequence(1).build(),
                        IssueTypeSchemeItemEntity.builder().issueTypeId(99L).sequence(2).build()
                ));
        when(projectReadPort.getActiveProjectIdsByIssueTypeSchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of(1000L));
        when(workItemReadPort.getActiveIssueTypeIdsInUseByProjectIds(TENANT_ID, List.of(1000L), List.of(99L)))
                .thenReturn(List.of(99L));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.replaceIssueTypeSchemeItems(SCHEME_ID, List.of(ISSUE_TYPE_ID), TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.ISSUE_TYPE_SCHEME_IN_USE, exception.getErrorCode());
        verify(issueTypeSchemeItemPort, never()).deleteIssueTypeSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID);
    }

    @Test
    void replaceIssueTypeSchemeItemsShouldPersistOrderedReplacement() {
        IssueTypeSchemeEntity existing = IssueTypeSchemeEntity.builder()
                .id(SCHEME_ID)
                .tenantId(TENANT_ID)
                .name("Tenant Scheme")
                .defaultIssueTypeId(ISSUE_TYPE_ID)
                .build();
        IssueTypeSchemeItemEntity first = IssueTypeSchemeItemEntity.builder().id(1L).issueTypeId(ISSUE_TYPE_ID).sequence(1).build();
        IssueTypeSchemeItemEntity second = IssueTypeSchemeItemEntity.builder().id(2L).issueTypeId(12L).sequence(2).build();

        when(issueTypeSchemePort.getIssueTypeSchemeById(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(existing));
        when(issueTypePort.getIssueTypesByIdsIncludingSystem(List.of(ISSUE_TYPE_ID, 12L), TENANT_ID))
                .thenReturn(List.of(
                        IssueTypeEntity.builder().id(ISSUE_TYPE_ID).tenantId(TENANT_ID).build(),
                        IssueTypeEntity.builder().id(12L).tenantId(TENANT_ID).build()
                ));
        when(issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID)).thenReturn(List.of());
        when(issueTypeSchemeItemPort.createIssueTypeSchemeItems(any())).thenReturn(List.of(first, second));

        IssueTypeSchemeEntity updated = service.replaceIssueTypeSchemeItems(
                SCHEME_ID,
                List.of(ISSUE_TYPE_ID, 12L),
                TENANT_ID,
                USER_ID
        );

        verify(issueTypeSchemeItemPort).deleteIssueTypeSchemeItemsBySchemeId(SCHEME_ID, TENANT_ID);
        verify(issueTypeSchemePort).updateIssueTypeScheme(existing);
        assertEquals(2, updated.getItems().size());
        assertEquals(ISSUE_TYPE_ID, updated.getItems().getFirst().getIssueTypeId());
        assertEquals(2, updated.getItems().get(1).getSequence());
        assertEquals(USER_ID, updated.getUpdatedBy());
    }
}
