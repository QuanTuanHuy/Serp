/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardCriteria;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardItemProjection;
import serp.project.pmcore.domain.workitem.dto.WorkItemBoardStatusProjection;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListWorkItemBoardQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;
    private static final Long PROJECT_ID = 6L;

    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IProjectService projectService;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;
    @Mock
    private IUserService userService;

    private ListWorkItemBoardQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListWorkItemBoardQueryHandler(
                workItemReadPort,
                projectService,
                projectPermissionEvaluationService,
                userService
        );
    }

    @Test
    void handleShouldSucceedWithUnassignedWorkItems() {
        WorkItemBoardCriteria criteria = WorkItemBoardCriteria.builder()
                .projectId(PROJECT_ID)
                .build();
        ListWorkItemBoardQuery query = new ListWorkItemBoardQuery(
                TENANT_ID,
                USER_ID,
                Set.of("dev-team"),
                criteria
        );

        when(projectService.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .build());

        WorkItemBoardStatusProjection status = WorkItemBoardStatusProjection.builder()
                .statusId(1L)
                .statusKey("TODO")
                .statusName("Todo Status")
                .build();
        when(workItemReadPort.listBoardStatuses(TENANT_ID, criteria)).thenReturn(List.of(status));

        WorkItemBoardItemProjection unassignedItem = WorkItemBoardItemProjection.builder()
                .id(101L)
                .projectId(PROJECT_ID)
                .key("SERP-101")
                .summary("Unassigned work item")
                .assigneeId(null)
                .assigneeName(null)
                .assigneeAvatarUrl(null)
                .statusId(1L)
                .build();
        when(workItemReadPort.listBoardWorkItems(TENANT_ID, criteria)).thenReturn(List.of(unassignedItem));

        WorkItemBoardView result = handler.handle(query);

        assertNotNull(result);
        assertEquals(PROJECT_ID, result.projectId());
        assertEquals(1, result.columns().size());

        WorkItemBoardColumnView column = result.columns().get(0);
        assertEquals(1L, column.statusId());
        assertEquals(1, column.items().size());

        WorkItemBoardCardView card = column.items().get(0);
        assertEquals(101L, card.id());
        assertEquals(PROJECT_ID, card.projectId());
        assertEquals("SERP-101", card.key());
        assertEquals("Unassigned work item", card.summary());
    }
}
