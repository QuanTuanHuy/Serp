/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.delete;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteWorkItemValidatorTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;

    @Mock
    private IProjectService projectService;

    @InjectMocks
    private DeleteWorkItemValidator validator;

    @Test
    void validateCommandShouldRejectNullCommand() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCommand(null)
        );

        assertEquals("Delete work item command is required", exception.getMessage());
    }

    @Test
    void validateCommandShouldRejectNonPositiveProjectId() {
        DeleteWorkItemCommand command = command(0L, WORK_ITEM_ID, TENANT_ID, USER_ID);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCommand(command)
        );

        assertEquals("projectId must be positive", exception.getMessage());
    }

    @Test
    void validateCommandShouldRejectNonPositiveWorkItemId() {
        DeleteWorkItemCommand command = command(PROJECT_ID, 0L, TENANT_ID, USER_ID);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCommand(command)
        );

        assertEquals("workItemId must be positive", exception.getMessage());
    }

    @Test
    void validateCommandShouldRejectNonPositiveTenantId() {
        DeleteWorkItemCommand command = command(PROJECT_ID, WORK_ITEM_ID, 0L, USER_ID);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCommand(command)
        );

        assertEquals("tenantId must be positive", exception.getMessage());
    }

    @Test
    void validateCommandShouldRejectNonPositiveUserId() {
        DeleteWorkItemCommand command = command(PROJECT_ID, WORK_ITEM_ID, TENANT_ID, 0L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateCommand(command)
        );

        assertEquals("userId must be positive", exception.getMessage());
    }

    @Test
    void validateWritableProjectShouldRejectArchivedProject() {
        ProjectEntity project = new ProjectEntity();
        setField(project, "id", PROJECT_ID);
        setField(project, "isArchived", true);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> validator.validateWritableProject(PROJECT_ID, TENANT_ID)
        );

        assertEquals(DomainErrorCode.PROJECT_ARCHIVED, getField(exception, "errorCode"));
    }

    @Test
    void validateWritableProjectShouldReturnProjectWhenNotArchived() {
        ProjectEntity project = new ProjectEntity();
        setField(project, "id", PROJECT_ID);
        setField(project, "isArchived", false);

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(project);

        ProjectEntity result = validator.validateWritableProject(PROJECT_ID, TENANT_ID);

        assertEquals(project, result);
        verify(projectService).getProjectById(PROJECT_ID, TENANT_ID);
    }

    private DeleteWorkItemCommand command(Long projectId, Long workItemId, Long tenantId, Long userId) {
        return new DeleteWorkItemCommand(
                projectId,
                workItemId,
                tenantId,
                userId,
                Set.of("dev")
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
