/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.dto.WorkItemDeleteExecutionResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemDeleteAuthorizationService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteWorkItemCommandHandlerTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;

    @Mock
    private IWorkItemService workItemService;

    @Mock
    private IWorkItemDeleteAuthorizationService workItemDeleteAuthorizationService;

    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;

    @Mock
    private DeleteWorkItemValidator deleteWorkItemValidator;

    @InjectMocks
    private DeleteWorkItemCommandHandler handler;

    @Test
    void handleShouldDeleteWorkItemWhenCommandIsValid() {
        DeleteWorkItemCommand command = command(Set.of("dev-team", "qa"));
        ProjectEntity project = new ProjectEntity();
        setField(project, "id", PROJECT_ID);
        setField(project, "tenantId", TENANT_ID);

        WorkItemEntity workItem = new WorkItemEntity();
        setField(workItem, "id", WORK_ITEM_ID);
        setField(workItem, "projectId", PROJECT_ID);

        WorkItemDeleteExecutionResult executionResult = new WorkItemDeleteExecutionResult(3, 4, 2);

        when(deleteWorkItemValidator.validateWritableProject(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(USER_ID, Set.of("dev-team", "qa"), null, null))
                .thenReturn(ProjectPermissionEvaluationContext.builder()
                        .userId(USER_ID)
                        .groupKeys(Set.of("dev-team", "qa"))
                        .build());
        when(workItemService.softDeleteWorkItem(
                eq(WORK_ITEM_ID),
                eq(PROJECT_ID),
                eq(TENANT_ID),
                eq(USER_ID),
                anyLong()
        )).thenReturn(executionResult);

        DeleteWorkItemResult result = handler.handle(command);

        assertEquals(WORK_ITEM_ID, result.rootWorkItemId());
        assertEquals(3, result.deletedWorkItemCount());
        assertEquals(4, result.deletedRelationCount());
        assertEquals(2, result.deletedLinkCount());

        ArgumentCaptor<ProjectPermissionEvaluationContext> permissionContextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(workItemDeleteAuthorizationService).checkDeletePermission(
                eq(project),
                permissionContextCaptor.capture()
        );
        ProjectPermissionEvaluationContext permissionContext = permissionContextCaptor.getValue();
        assertEquals(USER_ID, getField(permissionContext, "userId"));
        assertEquals(Set.of("dev-team", "qa"), getField(permissionContext, "groupKeys"));

        ArgumentCaptor<ProjectPermissionEvaluationContext> securityContextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(workItemDeleteAuthorizationService).checkDeleteSecurityAccess(
                eq(project),
                eq(workItem),
                securityContextCaptor.capture()
        );
        ProjectPermissionEvaluationContext securityContext = securityContextCaptor.getValue();
        assertEquals(USER_ID, getField(securityContext, "userId"));
        assertEquals(Set.of("dev-team", "qa"), getField(securityContext, "groupKeys"));

        ArgumentCaptor<Long> deletedAtCaptor = ArgumentCaptor.forClass(Long.class);
        verify(workItemService).softDeleteWorkItem(
                eq(WORK_ITEM_ID),
                eq(PROJECT_ID),
                eq(TENANT_ID),
                eq(USER_ID),
                deletedAtCaptor.capture()
        );
        assertEquals(deletedAtCaptor.getValue(), result.deletedAt());

        verify(deleteWorkItemValidator).validateCommand(command);
    }

    @Test
    void handleShouldRejectWhenWorkItemDoesNotBelongToProject() {
        DeleteWorkItemCommand command = command(Set.of("dev-team"));
        ProjectEntity project = new ProjectEntity();
        setField(project, "id", PROJECT_ID);
        setField(project, "tenantId", TENANT_ID);

        WorkItemEntity workItemFromOtherProject = new WorkItemEntity();
        setField(workItemFromOtherProject, "id", WORK_ITEM_ID);
        setField(workItemFromOtherProject, "projectId", 999L);

        when(deleteWorkItemValidator.validateWritableProject(PROJECT_ID, TENANT_ID)).thenReturn(project);
        when(workItemService.getWorkItemById(WORK_ITEM_ID, TENANT_ID)).thenReturn(workItemFromOtherProject);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> handler.handle(command)
        );

        assertEquals(DomainErrorCode.WORK_ITEM_NOT_FOUND, getField(exception, "errorCode"));

        verify(workItemDeleteAuthorizationService, never()).checkDeletePermission(any(), any());
        verify(workItemDeleteAuthorizationService, never()).checkDeleteSecurityAccess(any(), any(), any());
        verify(workItemService, never()).softDeleteWorkItem(anyLong(), anyLong(), anyLong(), anyLong(), anyLong());
    }

    private DeleteWorkItemCommand command(Set<String> groupKeys) {
        return new DeleteWorkItemCommand(
                PROJECT_ID,
                WORK_ITEM_ID,
                TENANT_ID,
                USER_ID,
                groupKeys
        );
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

    private Object getField(Object target, String fieldName) {
        try {
            Field field = resolveField(target.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read field: " + fieldName, e);
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
