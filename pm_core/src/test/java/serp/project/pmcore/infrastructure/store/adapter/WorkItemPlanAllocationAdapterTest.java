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
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanAllocationMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanAllocationModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanAllocationRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemPlanAllocationAdapterTest {
    private static final Long TENANT_ID = 2L;
    private static final Long PLAN_ID = 4L;

    @Mock
    private IWorkItemPlanAllocationRepository workItemPlanAllocationRepository;

    private WorkItemPlanAllocationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkItemPlanAllocationAdapter(
                workItemPlanAllocationRepository,
                new WorkItemPlanAllocationMapper()
        );
    }

    @Test
    void replaceForPlanShouldHardDeleteExistingAllocationsBeforeSavingNewOnes() {
        WorkItemPlanAllocationEntity allocation = WorkItemPlanAllocationEntity.builder()
                .tenantId(TENANT_ID)
                .projectId(3L)
                .workItemPlanId(PLAN_ID)
                .workItemId(5L)
                .assigneeId(6L)
                .startTime(1_700_000_000_000L)
                .endTime(1_700_003_600_000L)
                .effortMillis(3_600_000L)
                .source(WorkItemPlanSource.OPTIMIZATION)
                .sourceRunId(7L)
                .sourceRunItemId(8L)
                .build();
        WorkItemPlanAllocationModel saved = WorkItemPlanAllocationModel.builder()
                .id(40L)
                .tenantId(TENANT_ID)
                .projectId(3L)
                .workItemPlanId(PLAN_ID)
                .workItemId(5L)
                .assigneeId(6L)
                .startTime(java.time.LocalDateTime.of(2023, 11, 15, 5, 13, 20))
                .endTime(java.time.LocalDateTime.of(2023, 11, 15, 6, 13, 20))
                .effortMillis(3_600_000L)
                .source(WorkItemPlanSource.OPTIMIZATION)
                .sourceRunId(7L)
                .sourceRunItemId(8L)
                .build();
        when(workItemPlanAllocationRepository.saveAll(anyList())).thenReturn(List.of(saved));

        List<WorkItemPlanAllocationEntity> result = adapter.replaceForPlan(TENANT_ID, PLAN_ID, List.of(allocation));

        verify(workItemPlanAllocationRepository).deleteByTenantIdAndWorkItemPlanId(TENANT_ID, PLAN_ID);
        verify(workItemPlanAllocationRepository, never()).findAllByTenantIdAndWorkItemPlanId(TENANT_ID, PLAN_ID);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(40L);
    }

    @Test
    void replaceForPlanShouldOnlyHardDeleteWhenReplacementIsEmpty() {
        List<WorkItemPlanAllocationEntity> result = adapter.replaceForPlan(TENANT_ID, PLAN_ID, List.of());

        verify(workItemPlanAllocationRepository).deleteByTenantIdAndWorkItemPlanId(TENANT_ID, PLAN_ID);
        verify(workItemPlanAllocationRepository, never()).saveAll(anyList());
        assertThat(result).isEmpty();
    }
}
