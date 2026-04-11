/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.port.IProjectIssueCounterPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

    private static final Long TENANT_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long USER_ID = 99L;
    private static final Long DELETED_AT = 1_700_000_000_000L;

    @Mock
    private IWorkItemReadPort workItemReadPort;

    @Mock
    private IWorkItemWritePort workItemWritePort;

    @Mock
    private IProjectIssueCounterPort projectIssueCounterPort;

    @Mock
    private IIssueTypePort issueTypePort;

    @InjectMocks
    private WorkItemService workItemService;

    @Test
    void validateParentHierarchyShouldAllowSubtaskUnderStandard() {
        mockParent(10L, 100L, 200L, 1);
        mockChildIssueType(201L, 0);

        assertDoesNotThrow(() -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void validateParentHierarchyShouldRejectSubtaskUnderEpic() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 0);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void validateParentHierarchyShouldAllowStandardUnderEpic() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 1);

        assertDoesNotThrow(() -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void validateParentHierarchyShouldRejectStandardUnderStandard() {
        mockParent(10L, 100L, 200L, 1);
        mockChildIssueType(201L, 1);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void validateParentHierarchyShouldRejectParentFromAnotherProject() {
        WorkItemEntity parent = workItem(10L, 999L, 200L);
        when(workItemReadPort.getWorkItemById(10L, TENANT_ID)).thenReturn(Optional.of(parent));

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void validateParentHierarchyShouldRejectEpicWithParent() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 2);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, TENANT_ID));
    }

    @Test
    void softDeleteWorkItemShouldDeleteRootAndDescendantsInSingleScope() {
        WorkItemEntity root = workItem(1L, PROJECT_ID, 200L);
        WorkItemEntity childA = workItem(2L, PROJECT_ID, 201L);
        WorkItemEntity childB = workItem(3L, PROJECT_ID, 202L);
        WorkItemEntity grandChild = workItem(4L, PROJECT_ID, 203L);

        when(workItemReadPort.getWorkItemById(1L, TENANT_ID)).thenReturn(Optional.of(root));
        when(workItemReadPort.getActiveChildrenByParentId(1L, TENANT_ID)).thenReturn(List.of(childA, childB));
        when(workItemReadPort.getActiveChildrenByParentId(2L, TENANT_ID)).thenReturn(List.of(grandChild));
        when(workItemReadPort.getActiveChildrenByParentId(3L, TENANT_ID)).thenReturn(List.of());
        when(workItemReadPort.getActiveChildrenByParentId(4L, TENANT_ID)).thenReturn(List.of());

        WorkItemDeleteExecutionResult expected = new WorkItemDeleteExecutionResult(4, 7, 2);
        when(workItemWritePort.softDeleteWorkItems(
                eq(PROJECT_ID),
                eq(TENANT_ID),
                eq(Set.of(1L, 2L, 3L, 4L)),
                eq(USER_ID),
                eq(DELETED_AT)
        )).thenReturn(expected);

        WorkItemDeleteExecutionResult result = workItemService.softDeleteWorkItem(
                1L,
                PROJECT_ID,
                TENANT_ID,
                USER_ID,
                DELETED_AT
        );

        assertEquals(4, result.deletedWorkItemCount());
        assertEquals(7, result.deletedRelationCount());
        assertEquals(2, result.deletedLinkCount());

        ArgumentCaptor<Set<Long>> scopeCaptor = ArgumentCaptor.forClass(Set.class);
        verify(workItemWritePort).softDeleteWorkItems(
                eq(PROJECT_ID),
                eq(TENANT_ID),
                scopeCaptor.capture(),
                eq(USER_ID),
                eq(DELETED_AT)
        );
        assertEquals(Set.of(1L, 2L, 3L, 4L), scopeCaptor.getValue());
    }

    @Test
    void softDeleteWorkItemShouldThrowNotFoundWhenRootDoesNotBelongToProject() {
        WorkItemEntity rootFromAnotherProject = workItem(1L, 999L, 200L);
        when(workItemReadPort.getWorkItemById(1L, TENANT_ID)).thenReturn(Optional.of(rootFromAnotherProject));

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> workItemService.softDeleteWorkItem(1L, PROJECT_ID, TENANT_ID, USER_ID, DELETED_AT)
        );

        assertEquals("Work item not found", exception.getMessage());
        verify(workItemWritePort, never()).softDeleteWorkItems(
                anyLong(), anyLong(), org.mockito.ArgumentMatchers.anySet(), anyLong(), anyLong()
        );
    }

    @Test
    void softDeleteWorkItemShouldRejectCrossProjectChildInDeleteScope() {
        WorkItemEntity root = workItem(1L, PROJECT_ID, 200L);
        WorkItemEntity crossProjectChild = workItem(2L, 999L, 201L);

        when(workItemReadPort.getWorkItemById(1L, TENANT_ID)).thenReturn(Optional.of(root));
        when(workItemReadPort.getActiveChildrenByParentId(1L, TENANT_ID)).thenReturn(List.of(crossProjectChild));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> workItemService.softDeleteWorkItem(1L, PROJECT_ID, TENANT_ID, USER_ID, DELETED_AT)
        );

        assertTrue(exception.getMessage().contains("Cross-project child detected"));
    }

    @Test
    void softDeleteWorkItemShouldRejectCycleInDeleteScope() {
        WorkItemEntity root = workItem(1L, PROJECT_ID, 200L);
        WorkItemEntity child = workItem(2L, PROJECT_ID, 201L);

        when(workItemReadPort.getWorkItemById(1L, TENANT_ID)).thenReturn(Optional.of(root));
        when(workItemReadPort.getActiveChildrenByParentId(1L, TENANT_ID)).thenReturn(List.of(child));
        when(workItemReadPort.getActiveChildrenByParentId(2L, TENANT_ID)).thenReturn(List.of(root));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> workItemService.softDeleteWorkItem(1L, PROJECT_ID, TENANT_ID, USER_ID, DELETED_AT)
        );

        assertTrue(exception.getMessage().contains("Cycle detected while resolving delete scope"));
    }

    private void mockParent(Long parentId, Long projectId, Long issueTypeId, int hierarchyLevel) {
        WorkItemEntity parent = workItem(parentId, projectId, issueTypeId);
        when(workItemReadPort.getWorkItemById(parentId, TENANT_ID)).thenReturn(Optional.of(parent));

        IssueTypeEntity parentIssueType = issueType(issueTypeId, hierarchyLevel);
        when(issueTypePort.getIssueTypeById(issueTypeId, TENANT_ID)).thenReturn(Optional.of(parentIssueType));
    }

    private void mockChildIssueType(Long issueTypeId, int hierarchyLevel) {
        IssueTypeEntity childIssueType = issueType(issueTypeId, hierarchyLevel);
        when(issueTypePort.getIssueTypeById(issueTypeId, TENANT_ID)).thenReturn(Optional.of(childIssueType));
    }

    private WorkItemEntity workItem(Long id, Long projectId, Long issueTypeId) {
        WorkItemEntity workItem = new WorkItemEntity();
        setField(workItem, "id", id);
        setField(workItem, "projectId", projectId);
        setField(workItem, "issueTypeId", issueTypeId);
        return workItem;
    }

    private IssueTypeEntity issueType(Long id, Integer hierarchyLevel) {
        IssueTypeEntity issueType = new IssueTypeEntity();
        setField(issueType, "id", id);
        setField(issueType, "hierarchyLevel", hierarchyLevel);
        return issueType;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = resolveField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field: " + fieldName, e);
        }
    }

    private Field resolveField(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new IllegalArgumentException("Field not found: " + fieldName + " on " + type.getName());
    }
}
