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
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.WorkflowMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkflowRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowAdapterTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private IWorkflowRepository workflowRepository;

    private WorkflowAdapter workflowAdapter;

    @BeforeEach
    void setUp() {
        workflowAdapter = new WorkflowAdapter(workflowRepository, new WorkflowMapper());
    }

    @Test
    void listWorkflowsIncludingSystemShouldPassNullSearchPatternWhenSearchIsBlank() {
        WorkflowListCriteria criteria = WorkflowListCriteria.builder()
                .search("   ")
                .isActive(true)
                .isSystem(false)
                .sortBy("name")
                .sortDirection("ASC")
                .build();

        when(workflowRepository.findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq(null),
                eq(true),
                eq(false),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        workflowAdapter.listWorkflowsIncludingSystem(TENANT_ID, criteria);

        verify(workflowRepository).findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq(null),
                eq(true),
                eq(false),
                any(Pageable.class)
        );
    }

    @Test
    void listWorkflowsIncludingSystemShouldPassLowercaseSearchPattern() {
        WorkflowListCriteria criteria = WorkflowListCriteria.builder()
                .search(" Review ")
                .sortBy("name")
                .sortDirection("ASC")
                .build();

        when(workflowRepository.findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq("%review%"),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        workflowAdapter.listWorkflowsIncludingSystem(TENANT_ID, criteria);

        verify(workflowRepository).findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq("%review%"),
                eq(null),
                eq(null),
                any(Pageable.class)
        );
    }
}
