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
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;

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
class StatusCategoryServiceTest {

    private static final Long CATEGORY_ID = 10L;
    private static final Long TENANT_ID = 20L;
    private static final Long USER_ID = 30L;

    @Mock
    private IStatusCategoryPort statusCategoryPort;
    @Mock
    private IStatusPort statusPort;

    private StatusCategoryService service;

    @BeforeEach
    void setUp() {
        service = new StatusCategoryService(statusCategoryPort, statusPort);
    }

    @Test
    void createStatusCategoryShouldPersistTenantOwnedCategory() {
        StatusCategoryEntity draft = StatusCategoryEntity.builder()
                .name(" In Progress ")
                .key(" indeterminate ")
                .color(" yellow ")
                .build();

        when(statusCategoryPort.existsByKey(TENANT_ID, "indeterminate")).thenReturn(false);
        when(statusCategoryPort.createStatusCategory(any(StatusCategoryEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StatusCategoryEntity created = service.createStatusCategory(draft, TENANT_ID, USER_ID);

        ArgumentCaptor<StatusCategoryEntity> captor = ArgumentCaptor.forClass(StatusCategoryEntity.class);
        verify(statusCategoryPort).createStatusCategory(captor.capture());
        StatusCategoryEntity persisted = captor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals("In Progress", persisted.getName());
        assertEquals("indeterminate", persisted.getKey());
        assertEquals("yellow", persisted.getColor());
        assertEquals(false, persisted.getIsSystem());
        assertNotNull(persisted.getCreatedAt());
        assertSame(persisted, created);
    }

    @Test
    void getVisibleStatusCategoryByIdShouldReturnSystemCategory() {
        StatusCategoryEntity systemCategory = StatusCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(0L)
                .name("Done")
                .key("done")
                .color("green")
                .isSystem(true)
                .build();

        when(statusCategoryPort.getStatusCategoryByIdIncludingSystem(CATEGORY_ID, TENANT_ID))
                .thenReturn(Optional.of(systemCategory));

        StatusCategoryEntity visible = service.getVisibleStatusCategoryById(CATEGORY_ID, TENANT_ID);

        assertSame(systemCategory, visible);
        assertEquals(true, visible.getIsSystem());
    }

    @Test
    void updateStatusCategoryShouldTreatSystemCategoryAsNotFoundForWritePath() {
        when(statusCategoryPort.getStatusCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateStatusCategory(
                        CATEGORY_ID,
                        new StatusCategoryUpdateData("Done", true, null, false, null, false),
                        TENANT_ID,
                        USER_ID
                )
        );

        assertEquals(DomainErrorCode.STATUS_CATEGORY_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void listVisibleStatusCategoriesShouldDelegateToPort() {
        StatusCategoryListCriteria criteria = StatusCategoryListCriteria.builder()
                .search("done")
                .isSystem(true)
                .build();
        PageResult<StatusCategoryEntity> expected = new PageResult<>(
                List.of(StatusCategoryEntity.builder().id(1L).name("Done").build()),
                1L
        );
        when(statusCategoryPort.listStatusCategoriesIncludingSystem(TENANT_ID, criteria)).thenReturn(expected);

        PageResult<StatusCategoryEntity> result = service.listVisibleStatusCategories(TENANT_ID, criteria);

        assertSame(expected, result);
    }

    @Test
    void deleteStatusCategoryShouldRejectWhenStillReferencedByStatuses() {
        StatusCategoryEntity existing = StatusCategoryEntity.builder()
                .id(CATEGORY_ID)
                .tenantId(TENANT_ID)
                .name("In Progress")
                .key("indeterminate")
                .isSystem(false)
                .build();

        when(statusCategoryPort.getStatusCategoryById(CATEGORY_ID, TENANT_ID)).thenReturn(Optional.of(existing));
        when(statusPort.existsByCategoryId(CATEGORY_ID, TENANT_ID)).thenReturn(true);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> service.deleteStatusCategory(CATEGORY_ID, TENANT_ID, USER_ID)
        );

        assertEquals(DomainErrorCode.STATUS_CATEGORY_IN_USE, exception.getErrorCode());
        verify(statusCategoryPort, never()).updateStatusCategory(any(StatusCategoryEntity.class));
    }
}
