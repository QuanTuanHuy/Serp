/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.summary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryActivityProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryBreakdownProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryCriteria;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryMetricsProjection;
import serp.project.pmcore.domain.workitem.dto.ProjectSummaryParentOptionProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectSummaryQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IUserService userService;

    private GetProjectSummaryQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetProjectSummaryQueryHandler(
                projectReadPort,
                projectPermissionEvaluationService,
                workItemReadPort,
                userService
        );
    }

    @Test
    void handleShouldReturnSummaryAndApplyActivityPaging() {
        ProjectSummaryCriteria criteria = ProjectSummaryCriteria.builder()
                .projectId(PROJECT_ID)
                .statusIds(List.of(202L))
                .activityPage(1)
                .activitySize(2)
                .build();
        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .permissionSchemeId(100L)
                .build()));
        when(workItemReadPort.getProjectSummaryMetrics(eq(TENANT_ID), eq(criteria), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(new ProjectSummaryMetricsProjection(1L, 4L, 2L, 3L));
        when(workItemReadPort.listProjectSummaryStatuses(TENANT_ID, criteria)).thenReturn(List.of(
                new ProjectSummaryBreakdownProjection(201L, "todo", "To Do", null, null, 1, "new", "To Do", 3L),
                new ProjectSummaryBreakdownProjection(202L, "in_progress", "In Progress", null, null, 2, "indeterminate", "In Progress", 1L)
        ));
        when(workItemReadPort.listProjectSummaryPriorities(TENANT_ID, criteria)).thenReturn(List.of(
                new ProjectSummaryBreakdownProjection(400L, "highest", "Highest", null, "#C9372C", 1, null, null, 0L),
                new ProjectSummaryBreakdownProjection(402L, "medium", "Medium", null, "#B38600", 3, null, null, 4L)
        ));
        when(workItemReadPort.listProjectSummaryIssueTypes(TENANT_ID, criteria)).thenReturn(List.of(
                new ProjectSummaryBreakdownProjection(300L, "task", "Task", null, null, 1, null, null, 3L),
                new ProjectSummaryBreakdownProjection(301L, "epic", "Epic", null, null, 2, null, null, 0L)
        ));
        when(workItemReadPort.listProjectSummaryActivities(TENANT_ID, criteria)).thenReturn(new PageResult<>(List.of(
                new ProjectSummaryActivityProjection(
                        "comment-1",
                        "COMMENT",
                        501L,
                        1000L,
                        "KAN-1",
                        "Build summary",
                        202L,
                        "in_progress",
                        "In Progress",
                        "Looks good",
                        null,
                        null,
                        null,
                        null,
                        100L
                )
        ), 5L));
        when(workItemReadPort.listProjectSummaryAssigneeIds(TENANT_ID, PROJECT_ID)).thenReturn(List.of(502L));
        when(workItemReadPort.listProjectSummaryParentOptions(TENANT_ID, PROJECT_ID)).thenReturn(List.of(
                new ProjectSummaryParentOptionProjection(900L, "KAN-900", "Parent epic")
        ));
        when(userService.getUserProfilesByIds(List.of(501L, 502L))).thenReturn(List.of(
                UserProfileDto.builder().id(501L).firstName("Huy").lastName("Tran").build(),
                UserProfileDto.builder().id(502L).email("assignee@test.local").build()
        ));

        ProjectSummaryView result = handler.handle(new GetProjectSummaryQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                criteria
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(
                any(ProjectPermissionSubject.class),
                contextCaptor.capture(),
                eq(ProjectPermissionKeys.BROWSE_PROJECTS)
        );
        verify(workItemReadPort).listProjectSummaryActivities(TENANT_ID, criteria);

        assertEquals(USER_ID, contextCaptor.getValue().getUserId());
        assertEquals(Set.of("devs"), contextCaptor.getValue().getGroupKeys());
        assertEquals(PROJECT_ID, result.projectId());
        assertEquals(1L, result.metrics().completedLast7Days());
        assertEquals(4L, result.statusOverview().total());
        assertEquals(0L, result.priorityBreakdown().getFirst().count());
        assertEquals(5L, result.recentActivity().totalItems());
        assertEquals(3, result.recentActivity().totalPages());
        assertEquals("Huy Tran", result.recentActivity().items().getFirst().actor().displayName());
        assertEquals("assignee@test.local", result.filterOptions().assignees().getFirst().displayName());
        assertEquals("KAN-900", result.filterOptions().parents().getFirst().key());
    }

    @Test
    void handleShouldRejectMissingProject() {
        ProjectSummaryCriteria criteria = ProjectSummaryCriteria.builder()
                .projectId(PROJECT_ID)
                .build();
        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> handler.handle(new GetProjectSummaryQuery(
                TENANT_ID,
                USER_ID,
                Set.of(),
                criteria
        )));
        verify(workItemReadPort, never()).getProjectSummaryMetrics(any(), any(), any(), any(), any());
    }

    @Test
    void handleShouldPropagateBrowsePermissionFailure() {
        ProjectSummaryCriteria criteria = ProjectSummaryCriteria.builder()
                .projectId(PROJECT_ID)
                .build();
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build();
        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(project));
        doThrow(AccessDeniedException.projectPermission(ProjectPermissionKeys.BROWSE_PROJECTS, PROJECT_ID))
                .when(projectPermissionEvaluationService)
                .checkPermission(any(ProjectPermissionSubject.class), any(ProjectPermissionEvaluationContext.class), eq(ProjectPermissionKeys.BROWSE_PROJECTS));

        assertThrows(AccessDeniedException.class, () -> handler.handle(new GetProjectSummaryQuery(
                TENANT_ID,
                USER_ID,
                Set.of(),
                criteria
        )));
        verify(workItemReadPort, never()).listProjectSummaryActivities(any(), any());
    }
}
