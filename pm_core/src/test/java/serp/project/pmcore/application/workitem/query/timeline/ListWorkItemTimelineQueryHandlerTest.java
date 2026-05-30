/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineDependencyProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineItemProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListWorkItemTimelineQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IWorkItemPlanPort workItemPlanPort;

    private ListWorkItemTimelineQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListWorkItemTimelineQueryHandler(
                workItemReadPort,
                projectService,
                projectPermissionEvaluationService,
                workItemPlanPort
        );
    }

    @Test
    void handleShouldReturnItemsAndDependenciesForVisibleTimelineRows() {
        WorkItemTimelineCriteria criteria = WorkItemTimelineCriteria.builder()
                .projectId(PROJECT_ID)
                .page(0)
                .pageSize(2)
                .build();
        ListWorkItemTimelineQuery query = new ListWorkItemTimelineQuery(
                TENANT_ID,
                USER_ID,
                Set.of("dev-team"),
                criteria,
                true
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build());
        when(workItemReadPort.listTimelineWorkItems(TENANT_ID, criteria)).thenReturn(new PageResult<>(
                List.of(
                        WorkItemTimelineItemProjection.builder()
                                .id(101L)
                                .projectId(PROJECT_ID)
                                .parentId(null)
                                .key("SERP-101")
                                .summary("Epic")
                                .startDate(1_700_000_000_000L)
                                .dueDate(1_800_000_000_000L)
                                .unscheduled(false)
                                .hasChildren(true)
                                .rank("0|a")
                                .issueTypeId(1L)
                                .issueTypeName("Epic")
                                .issueTypeHierarchyLevel(2)
                                .statusId(11L)
                                .statusName("In Progress")
                                .priorityId(21L)
                                .priorityName("High")
                                .priorityColor("#ff0000")
                                .build(),
                        WorkItemTimelineItemProjection.builder()
                                .id(102L)
                                .projectId(PROJECT_ID)
                                .parentId(101L)
                                .key("SERP-102")
                                .summary("Story")
                                .startDate(1_710_000_000_000L)
                                .dueDate(1_720_000_000_000L)
                                .unscheduled(false)
                                .hasChildren(false)
                                .rank("0|b")
                                .issueTypeId(2L)
                                .issueTypeName("Story")
                                .issueTypeHierarchyLevel(1)
                                .statusId(11L)
                                .statusName("In Progress")
                                .priorityId(21L)
                                .priorityName("High")
                                .priorityColor("#ff0000")
                                .build()
                ),
                2
        ));
        when(workItemReadPort.listTimelineDependencies(TENANT_ID, PROJECT_ID, List.of(101L, 102L))).thenReturn(List.of(
                WorkItemTimelineDependencyProjection.builder()
                        .linkId(500L)
                        .sourceId(101L)
                        .targetId(102L)
                        .linkTypeId(7L)
                        .linkTypeName("Blocks")
                        .description("blocks")
                        .build()
        ));
        when(workItemPlanPort.listActivePlansByWorkItemIds(TENANT_ID, List.of(101L, 102L))).thenReturn(List.of(
                WorkItemPlanEntity.builder()
                        .tenantId(TENANT_ID)
                        .projectId(PROJECT_ID)
                        .workItemId(101L)
                        .plannedStart(1_700_000_000_000L)
                        .plannedEnd(1_710_000_000_000L)
                        .source(WorkItemPlanSource.OPTIMIZATION)
                        .sourceRunId(501L)
                        .locked(false)
                        .build(),
                WorkItemPlanEntity.builder()
                        .tenantId(TENANT_ID)
                        .projectId(PROJECT_ID)
                        .workItemId(102L)
                        .plannedStart(1_720_000_000_000L)
                        .plannedEnd(1_730_000_000_000L)
                        .source(WorkItemPlanSource.MANUAL)
                        .locked(true)
                        .build()
        ));

        WorkItemTimelinePageView result = handler.handle(query);

        assertEquals(2, result.items().size());
        assertEquals(1, result.dependencies().size());
        assertEquals(2, result.totalItems());
        assertEquals(1, result.totalPages());
        assertEquals("SERP-101", result.items().getFirst().key());
        assertEquals(1_700_000_000_000L, result.items().getFirst().schedule().plannedStart());
        assertEquals(1_710_000_000_000L, result.items().getFirst().schedule().plannedEnd());
        assertEquals("OPTIMIZATION", result.items().getFirst().schedule().source());
        assertEquals(500L, result.dependencies().getFirst().linkId());
        verify(projectPermissionEvaluationService).checkPermission(any(), any(), eq(ProjectPermissionKeys.BROWSE_PROJECTS));
        verify(workItemReadPort).listTimelineDependencies(TENANT_ID, PROJECT_ID, List.of(101L, 102L));
    }
}
