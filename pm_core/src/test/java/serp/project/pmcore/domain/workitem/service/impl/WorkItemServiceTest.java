/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.pmcore.domain.issyetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issyetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.port.IProjectIssueCounterPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

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

        assertDoesNotThrow(() -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    @Test
    void validateParentHierarchyShouldRejectSubtaskUnderEpic() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 0);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    @Test
    void validateParentHierarchyShouldAllowStandardUnderEpic() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 1);

        assertDoesNotThrow(() -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    @Test
    void validateParentHierarchyShouldRejectStandardUnderStandard() {
        mockParent(10L, 100L, 200L, 1);
        mockChildIssueType(201L, 1);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    @Test
    void validateParentHierarchyShouldRejectParentFromAnotherProject() {
        WorkItemEntity parent = WorkItemEntity.builder()
                .id(10L)
                .projectId(999L)
                .issueTypeId(200L)
                .build();
        when(workItemReadPort.getWorkItemById(10L, 1L)).thenReturn(Optional.of(parent));

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    @Test
    void validateParentHierarchyShouldRejectEpicWithParent() {
        mockParent(10L, 100L, 200L, 2);
        mockChildIssueType(201L, 2);

        assertThrows(BusinessRuleViolationException.class,
                () -> workItemService.validateParentHierarchy(10L, 201L, 100L, 1L));
    }

    private void mockParent(Long parentId, Long projectId, Long issueTypeId, int hierarchyLevel) {
        WorkItemEntity parent = WorkItemEntity.builder()
                .id(parentId)
                .projectId(projectId)
                .issueTypeId(issueTypeId)
                .build();
        when(workItemReadPort.getWorkItemById(parentId, 1L)).thenReturn(Optional.of(parent));

        IssueTypeEntity parentIssueType = IssueTypeEntity.builder()
                .id(issueTypeId)
                .hierarchyLevel(hierarchyLevel)
                .build();
        when(issueTypePort.getIssueTypeById(issueTypeId, 1L)).thenReturn(Optional.of(parentIssueType));
    }

    private void mockChildIssueType(Long issueTypeId, int hierarchyLevel) {
        IssueTypeEntity childIssueType = IssueTypeEntity.builder()
                .id(issueTypeId)
                .hierarchyLevel(hierarchyLevel)
                .build();
        when(issueTypePort.getIssueTypeById(issueTypeId, 1L)).thenReturn(Optional.of(childIssueType));
    }
}
