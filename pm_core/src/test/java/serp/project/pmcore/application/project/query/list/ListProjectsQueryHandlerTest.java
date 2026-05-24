/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.user.service.IUserService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProjectsQueryHandlerTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 99L;

    @Mock
    private IProjectReadPort projectReadPort;

    @Mock
    private IUserService userService;
    
    @Mock
    private IProjectCategoryService projectCategoryService;

    private ListProjectsQueryHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListProjectsQueryHandler(projectReadPort, userService, projectCategoryService);
    }

    @Test
    void handleShouldReturnPagedProjectSummaries() {
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of("dev-team"), "serp", 20L, "software", false, 1, 2, "name", "asc"))
                .thenReturn(new PageResult<>(List.of(
                        ProjectEntity.builder()
                                .id(10L)
                                .key("SERP")
                                .name("SERP Platform")
                                .projectTypeKey("software")
                                .categoryId(20L)
                                .isArchived(false)
                                .createdAt(1000L)
                                .updatedAt(2000L)
                                .build(),
                        ProjectEntity.builder()
                                .id(11L)
                                .key("CRM")
                                .name("CRM")
                                .projectTypeKey("software")
                                .categoryId(20L)
                                .isArchived(false)
                                .createdAt(1100L)
                                .updatedAt(2100L)
                                .build()
                ), 5L));

        when(projectCategoryService.getCategoriesByIds(anyList()))
                .thenReturn(List.of(ProjectCategoryEntity.builder().id(20L).name("Software").build()));

        PageView<ProjectSummaryView> response = handler.handle(new ListProjectsQuery(
                TENANT_ID,
                USER_ID,
                Set.of("dev-team"),
                "serp",
                20L,
                "software",
                false,
                1,
                2,
                "name",
                "asc"
        ));

        verify(projectReadPort).getProjects(TENANT_ID, USER_ID, Set.of("dev-team"), "serp", 20L, "software", false, 1, 2, "name", "asc");
        assertEquals(5L, response.totalItems());
        assertEquals(3, response.totalPages());
        assertEquals(1, response.currentPage());
        assertEquals(2, response.pageSize());
        assertEquals(2, response.items().size());
        assertEquals("SERP", response.items().getFirst().key());
        assertEquals("SERP Platform", response.items().getFirst().name());
    }

    @Test
    void handleShouldDefaultMissingGroupsToEmptySet() {
        when(projectReadPort.getProjects(TENANT_ID, USER_ID, Set.of(), null, null, null, null, 0, 20, "id", "desc"))
                .thenReturn(new PageResult<>(List.of(), 0L));

        handler.handle(new ListProjectsQuery(
                TENANT_ID,
                USER_ID,
                null,
                null,
                null,
                null,
                null,
                0,
                20,
                null,
                null
        ));

        verify(projectReadPort).getProjects(TENANT_ID, USER_ID, Set.of(), null, null, null, null, 0, 20, "id", "desc");
    }
}
