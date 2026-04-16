/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.project.dto.ProjectRoleUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.port.IProjectRolePort;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
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
class ProjectRoleServiceTest {

    private static final Long ROLE_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectRolePort projectRolePort;
    @Mock
    private IPermissionSchemeEntryPort permissionSchemeEntryPort;

    private ProjectRoleService service;

    @BeforeEach
    void setUp() {
        service = new ProjectRoleService(projectRolePort, permissionSchemeEntryPort);
    }

    @Test
    void createProjectRoleShouldPersistTenantOwnedRole() {
        ProjectRoleEntity draft = ProjectRoleEntity.builder()
                .name(" Developers ")
                .description(" Delivery team ")
                .build();

        when(projectRolePort.existsByNameAndTenantId("Developers", TENANT_ID)).thenReturn(false);
        when(projectRolePort.saveProjectRole(any(ProjectRoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectRoleEntity created = service.createProjectRole(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<ProjectRoleEntity> captor = ArgumentCaptor.forClass(ProjectRoleEntity.class);
        verify(projectRolePort).saveProjectRole(captor.capture());
        ProjectRoleEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Developers", persisted.getName());
        assertEquals("Delivery team", persisted.getDescription());
        assertEquals(false, persisted.getIsSystem());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void updateProjectRoleShouldApplyMutableFieldsAndAllowClearingDescription() {
        ProjectRoleEntity existing = ProjectRoleEntity.builder()
                .id(ROLE_ID)
                .tenantId(TENANT_ID)
                .name("Developers")
                .description("Old")
                .isSystem(false)
                .createdBy(USER_ID)
                .createdAt(100L)
                .build();

        when(projectRolePort.getProjectRoleById(ROLE_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectRolePort.existsByNameAndTenantId("QA", TENANT_ID)).thenReturn(false);
        when(projectRolePort.saveProjectRole(existing)).thenReturn(existing);

        ProjectRoleEntity updated = service.updateProjectRole(
                ROLE_ID,
                new ProjectRoleUpdateData("QA", true, null, true),
                TENANT_ID,
                USER_ID
        );

        verify(projectRolePort).saveProjectRole(existing);
        assertEquals("QA", updated.getName());
        assertNull(updated.getDescription());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void listVisibleProjectRolesShouldDelegateToPort() {
        ProjectRoleListCriteria criteria = ProjectRoleListCriteria.builder().search("dev").build();
        PageResult<ProjectRoleEntity> expected = new PageResult<>(List.of(ProjectRoleEntity.builder().id(1L).name("Developers").build()), 1L);
        when(projectRolePort.getProjectRolesIncludingSystem(TENANT_ID, criteria)).thenReturn(expected);

        PageResult<ProjectRoleEntity> result = service.listVisibleProjectRoles(TENANT_ID, criteria);

        assertSame(expected, result);
    }

    @Test
    void deleteProjectRoleShouldRejectWhenRoleIsUsedInPermissionSchemes() {
        ProjectRoleEntity existing = ProjectRoleEntity.builder()
                .id(ROLE_ID)
                .tenantId(TENANT_ID)
                .name("Developers")
                .isSystem(false)
                .build();

        when(projectRolePort.getProjectRoleById(ROLE_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(permissionSchemeEntryPort.existsByProjectRoleId(TENANT_ID, ROLE_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteProjectRole(ROLE_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.ROLE_IN_USE_BY_PERMISSION, exception.getErrorCode());
        verify(projectRolePort, never()).saveProjectRole(any(ProjectRoleEntity.class));
    }

    @Test
    void updateProjectRoleShouldRejectSystemRole() {
        ProjectRoleEntity existing = ProjectRoleEntity.builder()
                .id(ROLE_ID)
                .tenantId(TENANT_ID)
                .name("Administrators")
                .isSystem(true)
                .build();

        when(projectRolePort.getProjectRoleById(ROLE_ID, TENANT_ID)).thenReturn(Optional.of(existing));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.updateProjectRole(
                        ROLE_ID,
                        new ProjectRoleUpdateData("Admins", true, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.ROLE_IS_SYSTEM, exception.getErrorCode());
    }
}
