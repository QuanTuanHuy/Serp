/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.create;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.worklog.WorklogValidator;
import serp.project.pmcore.application.worklog.command.WorklogOutboxPublisher;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;
import serp.project.pmcore.domain.worklog.service.IWorklogAuthorizationService;
import serp.project.pmcore.domain.worklog.service.IWorklogService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateWorklogCommandHandlerTest {

    @Mock
    private WorklogValidator worklogValidator;
    @Mock
    private IProjectService projectService;
    @Mock
    private IWorkItemService workItemService;
    @Mock
    private IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    @Mock
    private IWorklogAuthorizationService worklogAuthorizationService;
    @Mock
    private IWorklogService worklogService;
    @Mock
    private WorklogOutboxPublisher worklogOutboxPublisher;

    private CreateWorklogCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateWorklogCommandHandler(
                worklogValidator,
                projectService,
                workItemService,
                workItemAuthorizationSupportService,
                worklogAuthorizationService,
                worklogService,
                worklogOutboxPublisher
        );
    }

    @Test
    void handleShouldCreateWorklogRefreshTotalsAndPublishOutbox() {
        CreateWorklogCommand command = new CreateWorklogCommand(
                10L,
                20L,
                600L,
                1_710_000_000_000L,
                "Worked on API",
                1L,
                99L,
                Set.of("dev-team")
        );
        ProjectEntity project = ProjectEntity.builder().id(10L).tenantId(1L).isArchived(false).build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .reporterId(77L)
                .assigneeId(88L)
                .timeOriginalEstimate(3600L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(99L)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(77L)
                .assigneeUserId(88L)
                .build();
        WorklogEntity createdWorklog = WorklogEntity.builder()
                .id(30L)
                .tenantId(1L)
                .workItemId(20L)
                .authorId(99L)
                .comment("Worked on API")
                .startDate(1_710_000_000_000L)
                .timeSpent(600L)
                .createdAt(1_710_000_001_000L)
                .createdBy(99L)
                .updatedAt(1_710_000_001_000L)
                .updatedBy(99L)
                .build();
        WorkItemEntity refreshedWorkItem = WorkItemEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .timeSpent(600L)
                .timeRemainingEstimate(3000L)
                .build();

        when(projectService.getProjectById(10L, 1L)).thenReturn(project);
        when(workItemService.getWorkItemById(20L, 1L)).thenReturn(workItem);
        when(workItemAuthorizationSupportService.buildActorContext(99L, Set.of("dev-team"), 77L, 88L))
                .thenReturn(actorContext);
        when(worklogService.createWorklog(any(WorklogEntity.class), eq(1L), eq(99L))).thenReturn(createdWorklog);
        when(worklogService.refreshWorkItemTimeTracking(workItem, 99L)).thenReturn(refreshedWorkItem);

        CreateWorklogResult result = handler.handle(command);

        assertEquals(30L, result.id());
        assertEquals(600L, result.timeSpent());
        assertEquals(600L, result.workItemTimeSpent());
        assertEquals(3000L, result.workItemTimeRemainingEstimate());
        verify(worklogAuthorizationService).checkCreateAccess(project, workItem, actorContext);
        verify(worklogOutboxPublisher).publishWorklogCreated(eq(1L), any());
    }
}
