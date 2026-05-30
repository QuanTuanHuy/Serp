/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.optimization.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemPlanMapper;
import serp.project.pmcore.infrastructure.store.model.WorkItemModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemPlanModel;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemPlanRepository;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemRepository;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FallbackResourceCapacityAdapterTest {
    private static final long START = 1_714_960_800_000L;
    private static final long FOUR_HOURS = 14_400_000L;
    private static final long EIGHT_HOURS = 28_800_000L;

    @Mock
    private IWorkItemPlanRepository workItemPlanRepository;

    @Mock
    private IWorkItemRepository workItemRepository;

    private final WorkItemPlanMapper workItemPlanMapper = new WorkItemPlanMapper();

    @Test
    void resolveCapacityShouldSubtractSameProjectAndCrossProjectPlansWithoutIdentityLeak() {
        FallbackResourceCapacityAdapter adapter = adapter();
        WorkItemPlanModel sameProjectPlan = plan(10L, START, START + FOUR_HOURS);
        WorkItemPlanModel crossProjectPlan = plan(20L, START + FOUR_HOURS, START + EIGHT_HOURS);
        when(workItemPlanRepository.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), any(LocalDateTime.class), any(LocalDateTime.class), eq(List.of(99L))))
                .thenReturn(List.of(sameProjectPlan, crossProjectPlan));
        when(workItemRepository.findAllByTenantIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(workItem(10L, 100L, 100L), workItem(20L, 200L, 100L)));

        CapacityResolutionResult result = adapter.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));

        assertEquals(CapacitySourceMode.FALLBACK_WITH_WORKLOAD, result.sourceMode());
        assertEquals(0L, result.slots().get(0).capacityMillis());
        assertEquals(EIGHT_HOURS, result.deductedWorkloadMillis());
        assertEquals(FOUR_HOURS, result.sameProjectOutsideScopeDeductedMillis());
        assertEquals(FOUR_HOURS, result.crossProjectDeductedMillis());
        assertEquals(1, result.workloadBuckets().size());
        assertEquals(100L, result.workloadBuckets().get(0).assigneeId());
        assertTrue(result.workloadBuckets().get(0).totalReservedMillis() > 0);
    }

    @Test
    void resolveCapacityShouldPassSelectedItemsAsExclusions() {
        FallbackResourceCapacityAdapter adapter = adapter();
        when(workItemPlanRepository.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), any(LocalDateTime.class), any(LocalDateTime.class), eq(List.of(99L))))
                .thenReturn(List.of());
        when(workItemRepository.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(List.of(99L)), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        adapter.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));

        verify(workItemPlanRepository).findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), any(LocalDateTime.class), any(LocalDateTime.class), eq(List.of(99L)));
        verify(workItemRepository).findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(List.of(99L)), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void resolveCapacityShouldDeductUnplannedAssignedWorkItemEstimates() {
        FallbackResourceCapacityAdapter adapter = adapter();
        WorkItemModel activeUnplannedItem = workItem(40L, 200L, 100L);
        activeUnplannedItem.setTimeRemainingEstimate(120L);
        when(workItemPlanRepository.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());
        when(workItemRepository.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(activeUnplannedItem));

        CapacityResolutionResult result = adapter.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of());

        assertEquals(6 * 3_600_000L, result.slots().get(0).capacityMillis());
        assertEquals(2 * 3_600_000L, result.deductedWorkloadMillis());
        assertEquals(1, result.workloadBuckets().size());
        assertEquals(2 * 3_600_000L, result.workloadBuckets().get(0).crossProjectReservedMillis());
    }

    @Test
    void resolveCapacityShouldWarnWhenReservationsExceedAvailability() {
        FallbackResourceCapacityAdapter adapter = adapter();
        WorkItemPlanModel firstCrossProjectPlan = plan(20L, START, START + EIGHT_HOURS);
        WorkItemPlanModel secondCrossProjectPlan = plan(30L, START, START + EIGHT_HOURS);
        when(workItemPlanRepository.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(firstCrossProjectPlan, secondCrossProjectPlan));
        when(workItemRepository.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(workItemRepository.findAllByTenantIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(workItem(20L, 200L, 100L), workItem(30L, 300L, 100L)));

        CapacityResolutionResult result = adapter.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of());

        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.CAPACITY_RESERVATION_EXCEEDS_AVAILABILITY));
        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.CROSS_PROJECT_CAPACITY_CONFLICT));
    }

    private FallbackResourceCapacityAdapter adapter() {
        return new FallbackResourceCapacityAdapter(workItemPlanRepository, workItemRepository, workItemPlanMapper,
                new FallbackResourceCalendarAdapter());
    }

    private WorkItemPlanModel plan(Long workItemId, Long start, Long end) {
        return WorkItemPlanModel.builder()
                .tenantId(1L)
                .projectId(100L)
                .workItemId(workItemId)
                .plannedStart(LocalDateTime.ofInstant(Instant.ofEpochMilli(start), ZoneId.systemDefault()))
                .plannedEnd(LocalDateTime.ofInstant(Instant.ofEpochMilli(end), ZoneId.systemDefault()))
                .source(WorkItemPlanSource.OPTIMIZATION)
                .locked(false)
                .build();
    }

    private WorkItemModel workItem(Long id, Long projectId, Long assigneeId) {
        return WorkItemModel.builder()
                .id(id)
                .tenantId(1L)
                .projectId(projectId)
                .issueTypeId(1L)
                .issueNo(id)
                .key("SERP-" + id)
                .summary("Item " + id)
                .statusId(1L)
                .priorityId(1L)
                .reporterId(1L)
                .assigneeId(assigneeId)
                .build();
    }
}
