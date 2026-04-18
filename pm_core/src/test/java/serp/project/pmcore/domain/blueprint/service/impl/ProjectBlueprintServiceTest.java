/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;
import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.port.IBlueprintSchemeDefaultPort;
import serp.project.pmcore.domain.blueprint.port.IProjectBlueprintPort;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectBlueprintServiceTest {

    private static final Long BLUEPRINT_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectBlueprintPort projectBlueprintPort;
    @Mock
    private IBlueprintSchemeDefaultPort blueprintSchemeDefaultPort;

    private ProjectBlueprintService service;

    @BeforeEach
    void setUp() {
        service = new ProjectBlueprintService(projectBlueprintPort, blueprintSchemeDefaultPort);
    }

    @Test
    void createBlueprintShouldPersistTenantOwnedBlueprint() {
        ProjectBlueprintEntity draft = ProjectBlueprintEntity.builder()
                .name(" Software Template ")
                .description(" Default template ")
                .typeKey("software")
                .avatarUrl(" https://example.com/icon.png ")
                .build();

        when(projectBlueprintPort.existsByNameAndTenantId("Software Template", TENANT_ID)).thenReturn(false);
        when(projectBlueprintPort.saveBlueprint(any(ProjectBlueprintEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectBlueprintEntity created = service.createBlueprint(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<ProjectBlueprintEntity> captor = ArgumentCaptor.forClass(ProjectBlueprintEntity.class);
        verify(projectBlueprintPort).saveBlueprint(captor.capture());
        ProjectBlueprintEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Software Template", persisted.getName());
        assertEquals("software", persisted.getTypeKey());
        assertEquals("https://example.com/icon.png", persisted.getAvatarUrl());
        assertEquals(false, persisted.getIsSystem());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void createBlueprintShouldRejectInvalidProjectType() {
        ProjectBlueprintEntity draft = ProjectBlueprintEntity.builder()
                .name("Invalid Template")
                .typeKey("mobile")
                .build();

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.createBlueprint(draft, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.PROJECT_TYPE_INVALID, exception.getErrorCode());
    }

    @Test
    void updateBlueprintShouldApplyMutableFieldsAndAllowClearingOptionalFields() {
        ProjectBlueprintEntity existing = ProjectBlueprintEntity.builder()
                .id(BLUEPRINT_ID)
                .tenantId(TENANT_ID)
                .name("Software Template")
                .description("Old")
                .typeKey("software")
                .avatarUrl("https://example.com/old.png")
                .isSystem(false)
                .createdAt(100L)
                .createdBy(USER_ID)
                .build();

        when(projectBlueprintPort.getBlueprintByIdIncludingSystem(BLUEPRINT_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectBlueprintPort.existsByNameAndTenantId("Business Template", TENANT_ID)).thenReturn(false);
        when(projectBlueprintPort.saveBlueprint(existing)).thenReturn(existing);

        ProjectBlueprintEntity updated = service.updateBlueprint(
                BLUEPRINT_ID,
                new ProjectBlueprintUpdateData("Business Template", true, null, true, null, true),
                TENANT_ID,
                USER_ID
        );

        verify(projectBlueprintPort).saveBlueprint(existing);
        assertEquals("Business Template", updated.getName());
        assertNull(updated.getDescription());
        assertNull(updated.getAvatarUrl());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void getBlueprintDefaultsIncludingSystemShouldDelegateToPort() {
        List<BlueprintSchemeDefaultEntity> expected = List.of(
                BlueprintSchemeDefaultEntity.builder().id(1L).schemeType(SchemeType.WORKFLOW).schemeId(2L).build()
        );
        when(blueprintSchemeDefaultPort.getDefaultsByBlueprintIdIncludingSystem(BLUEPRINT_ID, TENANT_ID)).thenReturn(expected);

        List<BlueprintSchemeDefaultEntity> result = service.getBlueprintDefaultsIncludingSystem(BLUEPRINT_ID, TENANT_ID);

        assertSame(expected, result);
    }

    @Test
    void listBlueprintsIncludingSystemShouldDelegateToPort() {
        ProjectBlueprintListCriteria criteria = ProjectBlueprintListCriteria.builder().search("soft").build();
        PageResult<ProjectBlueprintEntity> expected = new PageResult<>(List.of(ProjectBlueprintEntity.builder().id(1L).name("Software Template").build()), 1L);
        when(projectBlueprintPort.listBlueprintsIncludingSystem(TENANT_ID, criteria)).thenReturn(expected);

        PageResult<ProjectBlueprintEntity> result = service.listBlueprintsIncludingSystem(TENANT_ID, criteria);

        assertSame(expected, result);
    }

    @Test
    void deleteBlueprintShouldRejectSystemBlueprint() {
        ProjectBlueprintEntity existing = ProjectBlueprintEntity.builder()
                .id(BLUEPRINT_ID)
                .tenantId(0L)
                .name("System Template")
                .typeKey("software")
                .isSystem(true)
                .build();

        when(projectBlueprintPort.getBlueprintByIdIncludingSystem(BLUEPRINT_ID, TENANT_ID)).thenReturn(Optional.of(existing));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteBlueprint(BLUEPRINT_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.BLUEPRINT_IS_SYSTEM, exception.getErrorCode());
        verify(projectBlueprintPort, never()).saveBlueprint(any(ProjectBlueprintEntity.class));
    }
}
