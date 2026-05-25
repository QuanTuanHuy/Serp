/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAdapterTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 10L;

    @Mock
    private IProjectRepository projectRepository;

    private ProjectAdapter projectAdapter;

    @BeforeEach
    void setUp() {
        projectAdapter = new ProjectAdapter(projectRepository, new ProjectMapper());
    }

    @Test
    void getProjectsShouldCallVisibleRepositoryQueryWithNormalizedGroups() {
        ProjectModel model = ProjectModel.builder()
                .id(100L)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Platform")
                .projectTypeKey("software")
                .archived(false)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 2, 0, 0))
                .build();

        when(projectRepository.findVisibleProjectsWithFilters(
                eq(TENANT_ID),
                eq(USER_ID),
                eq(",dev,team-a,"),
                eq("serp"),
                eq(20L),
                eq("software"),
                eq(false),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(model)));

        PageResult<ProjectEntity> result = projectAdapter.getProjects(
                TENANT_ID,
                USER_ID,
                Set.of(" Team-A ", "DEV"),
                "serp",
                20L,
                "software",
                false,
                0,
                10,
                "name",
                "asc"
        );

        verify(projectRepository).findVisibleProjectsWithFilters(
                eq(TENANT_ID),
                eq(USER_ID),
                eq(",dev,team-a,"),
                eq("serp"),
                eq(20L),
                eq("software"),
                eq(false),
                any(Pageable.class)
        );
        assertEquals(1L, result.total());
        assertEquals(1, result.items().size());
        assertEquals("SERP", result.items().getFirst().getKey());
    }
}
