/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQuery;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.search.WorkItemSearchView;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmGlobalSearchQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;

    @Mock
    private IProjectReadPort projectReadPort;

    @Mock
    private IWorkItemReadPort workItemReadPort;

    @Mock
    private SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;

    private PmGlobalSearchQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PmGlobalSearchQueryHandler(
                projectReadPort,
                workItemReadPort,
                searchWorkItemsQueryHandler
        );
    }

    @Test
    void handleShouldReturnEmptyGroupsForBlankOrShortQuery() {
        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                " s ",
                5,
                10L
        ));

        assertEquals("s", response.query());
        assertEquals(5, response.limit());
        assertTrue(response.groups().isEmpty());
        verify(projectReadPort, never()).getProjects(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
        verify(workItemReadPort, never()).searchVisibleWorkItems(any());
    }

    @Test
    void handleShouldOrderCurrentProjectWorkItemsThenGlobalWorkItemsThenProjects() {
        WorkItemSearchView currentItem = new WorkItemSearchView(
                100L, 10L, 1L, 7L, "SERP-7", "Search from current project", null,
                null, 1L, 1L, null, null, null, null, null, null, null, null,
                null, null, null, 1000L, null, 2000L, null, "Task", null, null,
                "High", null, null, null, "open", "Open", null, "backlog", "Backlog",
                null, null, null, null
        );
        when(searchWorkItemsQueryHandler.handle(any(SearchWorkItemsQuery.class)))
                .thenReturn(new PageView<>(List.of(currentItem), 1L, 1, 0, 3));
        when(workItemReadPort.searchVisibleWorkItems(any())).thenReturn(List.of(
                WorkItemEntity.builder()
                        .id(101L)
                        .projectId(11L)
                        .key("CORE-1")
                        .summary("Search from another project")
                        .statusName("In Progress")
                        .updatedAt(3000L)
                        .build()
        ));
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of("devs"), "search", null, null, false, 0, 3, "name", "asc"))
                .thenReturn(new PageResult<>(List.of(
                        ProjectEntity.builder()
                                .id(20L)
                                .key("SERP")
                                .name("SERP Platform")
                                .isArchived(false)
                                .updatedAt(4000L)
                                .build()
                ), 1L));

        PmGlobalSearchResponseView response = handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                " search ",
                3,
                10L
        ));

        assertEquals("search", response.query());
        assertEquals(3, response.limit());
        assertEquals(3, response.groups().size());
        assertEquals(PmGlobalSearchType.CURRENT_PROJECT_WORK_ITEM, response.groups().get(0).type());
        assertEquals(PmGlobalSearchType.WORK_ITEM, response.groups().get(1).type());
        assertEquals(PmGlobalSearchType.PROJECT, response.groups().get(2).type());
        assertEquals("/pm/projects/10/work-items/100", response.groups().get(0).items().getFirst().url());
        assertEquals("/pm/projects/11/work-items/101", response.groups().get(1).items().getFirst().url());
        assertEquals("/pm/projects/20/summary", response.groups().get(2).items().getFirst().url());
    }

    @Test
    void handleShouldClampLimitAndExcludeCurrentProjectFromGlobalWorkItems() {
        when(searchWorkItemsQueryHandler.handle(any(SearchWorkItemsQuery.class)))
                .thenReturn(new PageView<>(List.of(), 0L, 0, 0, 10));
        when(workItemReadPort.searchVisibleWorkItems(any())).thenReturn(List.of());
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of(), "abc", null, null, false, 0, 10, "name", "asc"))
                .thenReturn(new PageResult<>(List.of(), 0L));

        handler.handle(new PmGlobalSearchQuery(
                TENANT_ID,
                USER_ID,
                null,
                "abc",
                99,
                10L
        ));

        ArgumentCaptor<serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria> captor =
                ArgumentCaptor.forClass(serp.project.pmcore.domain.workitem.dto.VisibleWorkItemSearchCriteria.class);
        verify(workItemReadPort).searchVisibleWorkItems(captor.capture());
        assertEquals(10, captor.getValue().limit());
        assertEquals(10L, captor.getValue().excludedProjectId());
    }
}
