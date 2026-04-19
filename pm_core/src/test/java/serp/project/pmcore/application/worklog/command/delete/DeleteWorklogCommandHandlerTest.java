/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.command.delete;

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
class DeleteWorklogCommandHandlerTest {

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

    private DeleteWorklogCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteWorklogCommandHandler(
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
    void handleShouldDeleteWorklogRefreshTotalsAndPublishOutbox() {
        DeleteWorklogCommand command = new DeleteWorklogCommand(
                10L,
                20L,
                30L,
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
        WorklogEntity worklog = WorklogEntity.builder()
                .id(30L)
                .tenantId(1L)
                .workItemId(20L)
                .authorId(99L)
                .timeSpent(600L)
                .build();
        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(99L)
                .groupKeys(Set.of("dev-team"))
                .reporterUserId(77L)
                .assigneeUserId(88L)
                .build();
        WorklogEntity deletedWorklog = WorklogEntity.builder()
                .id(30L)
                .tenantId(1L)
                .workItemId(20L)
                .authorId(99L)
                .timeSpent(600L)
                .deletedAt(1_710_000_001_000L)
                .build();
        WorkItemEntity refreshedWorkItem = WorkItemEntity.builder()
                .id(20L)
                .tenantId(1L)
                .projectId(10L)
                .timeSpent(0L)
                .timeRemainingEstimate(3600L)
                .build();

        when(projectService.getProjectById(10L, 1L)).thenReturn(project);
        when(workItemService.getWorkItemById(20L, 1L)).thenReturn(workItem);
        when(worklogService.getWorklogById(30L, 1L)).thenReturn(worklog);
        when(workItemAuthorizationSupportService.buildActorContext(99L, Set.of("dev-team"), 77L, 88L))
                .thenReturn(actorContext);
        when(worklogService.softDeleteWorklog(eq(worklog), eq(99L), any(Long.class))).thenReturn(deletedWorklog);
        when(worklogService.refreshWorkItemTimeTracking(workItem, 99L)).thenReturn(refreshedWorkItem);

        DeleteWorklogResult result = handler.handle(command);

        assertEquals(30L, result.worklogId());
        assertEquals(0L, result.workItemTimeSpent());
        assertEquals(3600L, result.workItemTimeRemainingEstimate());
        verify(worklogAuthorizationService).checkDeleteAccess(project, workItem, worklog, actorContext);
        verify(worklogOutboxPublisher).publishWorklogDeleted(eq(1L), any());
    }
}
