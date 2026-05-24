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
import serp.project.pmcore.infrastructure.store.mapper.ProjectComponentMapper;
import serp.project.pmcore.infrastructure.store.repository.IProjectComponentRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectComponentAdapterTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long TENANT_ID = 1L;

    @Mock
    private IProjectComponentRepository projectComponentRepository;

    private ProjectComponentAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProjectComponentAdapter(projectComponentRepository, new ProjectComponentMapper());
    }

    @Test
    void countActiveIssuesByComponentIdsShouldReturnRepositoryCounts() {
        when(projectComponentRepository.countActiveIssuesByComponentIds(PROJECT_ID, TENANT_ID, List.of(20L, 21L)))
                .thenReturn(List.of(
                        projection(20L, 3L),
                        projection(21L, 0L)
                ));

        Map<Long, Long> result = adapter.countActiveIssuesByComponentIds(PROJECT_ID, TENANT_ID, List.of(20L, 21L));

        assertEquals(3L, result.get(20L));
        assertEquals(0L, result.get(21L));
    }

    @Test
    void countActiveIssuesByComponentIdsShouldSkipRepositoryWhenIdsEmpty() {
        Map<Long, Long> result = adapter.countActiveIssuesByComponentIds(PROJECT_ID, TENANT_ID, List.of());

        assertEquals(Map.of(), result);
        verify(projectComponentRepository, never()).countActiveIssuesByComponentIds(PROJECT_ID, TENANT_ID, List.of());
    }

    private IProjectComponentRepository.ComponentIssueCountProjection projection(Long componentId, Long issueCount) {
        return new IProjectComponentRepository.ComponentIssueCountProjection() {
            @Override
            public Long getComponentId() {
                return componentId;
            }

            @Override
            public Long getIssueCount() {
                return issueCount;
            }
        };
    }
}
