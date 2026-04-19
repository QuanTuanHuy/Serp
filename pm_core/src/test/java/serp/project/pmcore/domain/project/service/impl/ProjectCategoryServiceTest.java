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
import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
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
class ProjectCategoryServiceTest {

    private static final Long CATEGORY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IProjectCategoryPort projectCategoryPort;
    @Mock
    private IProjectReadPort projectReadPort;

    private ProjectCategoryService service;

    @BeforeEach
    void setUp() {
        service = new ProjectCategoryService(projectCategoryPort, projectReadPort);
    }

    @Test
    void createCategoryShouldPersistTenantOwnedCategory() {
        ProjectCategoryEntity draft = ProjectCategoryEntity.builder()
                .name(" Software ")
                .description(" Product delivery ")
                .build();

        when(projectCategoryPort.existsByNameAndTenantId("Software", TENANT_ID)).thenReturn(false);
        when(projectCategoryPort.createCategory(any(ProjectCategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectCategoryEntity created = service.createCategory(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<ProjectCategoryEntity> captor = ArgumentCaptor.forClass(ProjectCategoryEntity.class);
        verify(projectCategoryPort).createCategory(captor.capture());
        ProjectCategoryEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("Software", persisted.getName());
        assertEquals("Product delivery", persisted.getDescription());
        assertEquals(false, persisted.getIsSystem());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void updateCategoryShouldApplyMutableFieldsAndAllowClearingDescription() {
        ProjectCategoryEntity existing = ProjectCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(TENANT_ID)
                .name("Software")
                .description("Old")
                .isSystem(false)
                .createdBy(USER_ID)
                .createdAt(100L)
                .build();

        when(projectCategoryPort.getCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectCategoryPort.existsByNameAndTenantId("Business", TENANT_ID)).thenReturn(false);

        ProjectCategoryEntity updated = service.updateCategory(
                CATEGORY_ID,
                new ProjectCategoryUpdateData("Business", true, null, true),
                TENANT_ID,
                USER_ID
        );

        verify(projectCategoryPort).updateCategory(existing);
        assertEquals("Business", updated.getName());
        assertNull(updated.getDescription());
        assertEquals(USER_ID, updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void listCategoriesShouldDelegateToPort() {
        ProjectCategoryListCriteria criteria = ProjectCategoryListCriteria.builder().search("soft").build();
        PageResult<ProjectCategoryEntity> expected = new PageResult<>(List.of(ProjectCategoryEntity.builder().id(1L).name("Software").build()), 1L);
        when(projectCategoryPort.listCategories(TENANT_ID, criteria)).thenReturn(expected);

        PageResult<ProjectCategoryEntity> result = service.listCategories(TENANT_ID, criteria);

        assertSame(expected, result);
    }

    @Test
    void deleteCategoryShouldRejectWhenCategoryIsStillInUse() {
        ProjectCategoryEntity existing = ProjectCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(TENANT_ID)
                .name("Software")
                .isSystem(false)
                .build();

        when(projectCategoryPort.getCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(projectReadPort.existsActiveProjectByCategoryId(CATEGORY_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteCategory(CATEGORY_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.CATEGORY_IN_USE, exception.getErrorCode());
        verify(projectCategoryPort, never()).updateCategory(any(ProjectCategoryEntity.class));
    }
}
