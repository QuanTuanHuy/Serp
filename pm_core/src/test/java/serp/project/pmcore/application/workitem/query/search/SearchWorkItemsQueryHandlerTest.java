/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchWorkItemsQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IProjectReadPort projectReadPort;
    @Mock
    private IProjectPermissionEvaluationService projectPermissionEvaluationService;

    private SearchWorkItemsQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SearchWorkItemsQueryHandler(
                workItemReadPort,
                projectReadPort,
                projectPermissionEvaluationService
        );
    }

    @Test
    void handleShouldCheckBrowsePermissionAndReturnPagedSearchResults() {
        WorkItemSearchCriteria criteria = WorkItemSearchCriteria.builder()
                .projectId(PROJECT_ID)
                .keyword("serp")
                .enriched(true)
                .page(1)
                .pageSize(2)
                .build();

        when(projectReadPort.getProjectById(PROJECT_ID, TENANT_ID)).thenReturn(Optional.of(
                ProjectEntity.builder()
                        .id(PROJECT_ID)
                        .tenantId(TENANT_ID)
                        .permissionSchemeId(100L)
                        .build()
        ));
        when(workItemReadPort.searchWorkItems(TENANT_ID, criteria)).thenReturn(new PageResult<>(List.of(
                WorkItemEntity.builder()
                        .id(1000L)
                        .projectId(PROJECT_ID)
                        .issueTypeId(20L)
                        .issueNo(1L)
                        .key("SERP-1")
                        .summary("Search item")
                        .priorityId(30L)
                        .priorityName("High")
                        .issueTypeName("Task")
                        .createdAt(100L)
                        .updatedAt(200L)
                        .build(),
                WorkItemEntity.builder()
                        .id(1001L)
                        .projectId(PROJECT_ID)
                        .issueTypeId(21L)
                        .issueNo(2L)
                        .key("SERP-2")
                        .summary("Another item")
                        .createdAt(101L)
                        .updatedAt(201L)
                        .build()
        ), 5L));

        PageView<WorkItemSearchView> response = handler.handle(new SearchWorkItemsQuery(
                TENANT_ID,
                USER_ID,
                Set.of("devs"),
                criteria
        ));

        ArgumentCaptor<ProjectPermissionEvaluationContext> contextCaptor =
                ArgumentCaptor.forClass(ProjectPermissionEvaluationContext.class);
        verify(projectPermissionEvaluationService).checkPermission(any(ProjectPermissionSubject.class), contextCaptor.capture(), eq("BROWSE_PROJECTS"));
        verify(workItemReadPort).searchWorkItems(TENANT_ID, criteria);

        ProjectPermissionEvaluationContext context = contextCaptor.getValue();
        assertEquals(USER_ID, context.getUserId());
        assertEquals(Set.of("devs"), context.getGroupKeys());

        assertEquals(5L, response.totalItems());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.currentPage());
        assertEquals(2, response.pageSize());
        assertEquals(2, response.items().size());
        assertEquals("SERP-1", response.items().getFirst().key());
        assertEquals("High", response.items().getFirst().priorityName());
        assertEquals("Task", response.items().getFirst().issueTypeName());
    }
}
