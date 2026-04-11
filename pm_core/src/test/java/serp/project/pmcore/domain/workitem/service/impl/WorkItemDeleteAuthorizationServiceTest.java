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
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;

import java.lang.reflect.Field;
import java.util.Set;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkItemDeleteAuthorizationServiceTest {

    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IIssueSecurityService issueSecurityService;

    @InjectMocks
    private WorkItemDeleteAuthorizationService service;

    @Test
    void checkDeletePermissionShouldRequireBrowseAndDelete() {
        ProjectEntity project = project();
        ProjectPermissionEvaluationContext actorContext = actorContext();

        service.checkDeletePermission(project, actorContext);

        verify(workItemAuthorizationSupportService).checkRequiredPermissions(
                ProjectPermissionSubject.from(project),
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.DELETE_ISSUES
        );
    }

    @Test
    void checkDeleteSecurityAccessShouldDelegateToIssueSecurityService() {
        ProjectEntity project = project();
        WorkItemEntity workItem = workItem();
        ProjectPermissionEvaluationContext actorContext = actorContext();

        service.checkDeleteSecurityAccess(project, workItem, actorContext);

        verify(issueSecurityService).checkSecurityAccessIfNeeded(project, workItem, actorContext, 1L);
    }

    private ProjectEntity project() {
        ProjectEntity project = new ProjectEntity();
        setField(project, "tenantId", 1L);
        return project;
    }

    private WorkItemEntity workItem() {
        return new WorkItemEntity();
    }

    private ProjectPermissionEvaluationContext actorContext() {
        ProjectPermissionEvaluationContext context = new ProjectPermissionEvaluationContext();
        setField(context, "userId", 99L);
        setField(context, "groupKeys", Set.of("dev-team"));
        return context;
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
