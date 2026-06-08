/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.optimization.enums.CapacityCoverageStatus;
import serp.project.pmcore.domain.optimization.enums.CapacitySourceMode;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;
import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadAllocation;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadItem;
import serp.project.pmcore.domain.optimization.model.ResourceWorkloadPlan;
import serp.project.pmcore.domain.optimization.port.IResourceWorkloadReadPort;
import serp.project.pmcore.domain.optimization.service.impl.ResourceCapacityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResourceCapacityServiceTest {
    private static final long START = 1_714_960_800_000L;
    private static final long FOUR_HOURS = 14_400_000L;
    private static final long EIGHT_HOURS = 28_800_000L;

    private final IResourceWorkloadReadPort workloadReadPort = mock(IResourceWorkloadReadPort.class);

    @Test
    void resolveCapacityShouldReturnCalendarSlotsWhenNoWorkloadExists() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.MISSING));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of());
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of());

        CapacityResolutionResult result = service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));

        assertEquals(CapacitySourceMode.FALLBACK_WEEKDAY_8H_UTC_PLUS_7, result.sourceMode());
        assertEquals(EIGHT_HOURS, result.slots().getFirst().capacityMillis());
        assertEquals(0L, result.deductedWorkloadMillis());
        assertTrue(result.workloadBuckets().isEmpty());
    }

    @Test
    void resolveCapacityShouldSubtractSameProjectAndCrossProjectPlansWithoutIdentityLeak() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.MISSING));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of(
                        plan(10L, START, START + FOUR_HOURS),
                        plan(20L, START + FOUR_HOURS, START + EIGHT_HOURS)
                ));
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of());
        when(workloadReadPort.findWorkItemsByIds(eq(1L), anyList()))
                .thenReturn(List.of(
                        item(10L, 100L, 100L),
                        item(20L, 200L, 100L)
                ));

        CapacityResolutionResult result = service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));

        assertEquals(CapacitySourceMode.FALLBACK_WITH_WORKLOAD, result.sourceMode());
        assertEquals(0L, result.slots().getFirst().capacityMillis());
        assertEquals(EIGHT_HOURS, result.deductedWorkloadMillis());
        assertEquals(FOUR_HOURS, result.sameProjectOutsideScopeDeductedMillis());
        assertEquals(FOUR_HOURS, result.crossProjectDeductedMillis());
        assertEquals(1, result.workloadBuckets().size());
    }

    @Test
    void resolveCapacityShouldDeductUnplannedAssignedWorkItemEstimates() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.FULL));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of());
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of(itemWithEstimate(40L, 200L, 100L, 120L)));

        CapacityResolutionResult result = service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of());

        assertEquals(CapacitySourceMode.REAL_CALENDAR_WITH_WORKLOAD, result.sourceMode());
        assertEquals(6 * 3_600_000L, result.slots().getFirst().capacityMillis());
        assertEquals(2 * 3_600_000L, result.deductedWorkloadMillis());
        assertEquals(2 * 3_600_000L, result.workloadBuckets().getFirst().crossProjectReservedMillis());
    }

    @Test
    void resolveCapacityShouldDeductAllocationChunksBeforePlanSummaryRange() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.FULL));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of(planWithId(900L, 20L, START, START + 5 * EIGHT_HOURS)));
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of());
        when(workloadReadPort.findWorkItemsByIds(eq(1L), anyList()))
                .thenReturn(List.of(item(20L, 200L, 100L)));
        when(workloadReadPort.findAllocationsByPlanIds(1L, List.of(900L)))
                .thenReturn(List.of(new ResourceWorkloadAllocation(900L, 20L, 100L, START, START + FOUR_HOURS, FOUR_HOURS)));

        CapacityResolutionResult result = service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of());

        assertEquals(FOUR_HOURS, result.slots().getFirst().capacityMillis());
        assertEquals(FOUR_HOURS, result.deductedWorkloadMillis());
        assertEquals(FOUR_HOURS, result.workloadBuckets().getFirst().crossProjectReservedMillis());
    }

    @Test
    void resolveCapacityShouldPassSelectedItemsAsExclusions() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.MISSING));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of());
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), eq(List.of(99L))))
                .thenReturn(List.of());

        service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));

        verify(workloadReadPort).findActiveWorkloadPlans(1L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));
        verify(workloadReadPort).findActiveUnplannedWorkloadItems(1L, List.of(100L), START, START + EIGHT_HOURS, List.of(99L));
    }

    @Test
    void resolveCapacityShouldWarnWhenReservationsExceedAvailability() {
        ResourceCapacityService service = service(calendar(CapacityCoverageStatus.FULL));
        when(workloadReadPort.findActiveWorkloadPlans(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of(
                        plan(20L, START, START + EIGHT_HOURS),
                        plan(30L, START, START + EIGHT_HOURS)
                ));
        when(workloadReadPort.findActiveUnplannedWorkloadItems(eq(1L), eq(List.of(100L)), eq(START), eq(START + EIGHT_HOURS), anyList()))
                .thenReturn(List.of());
        when(workloadReadPort.findWorkItemsByIds(eq(1L), anyList()))
                .thenReturn(List.of(
                        item(20L, 200L, 100L),
                        item(30L, 300L, 100L)
                ));

        CapacityResolutionResult result = service.resolveCapacity(1L, 100L, List.of(100L), START, START + EIGHT_HOURS, List.of());

        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.CAPACITY_RESERVATION_EXCEEDS_AVAILABILITY));
        assertTrue(result.warnings().stream().anyMatch(warning -> warning.code() == OptimizationWarningCode.CROSS_PROJECT_CAPACITY_CONFLICT));
    }

    private ResourceCapacityService service(IResourceCalendarService calendarService) {
        return new ResourceCapacityService(calendarService, workloadReadPort);
    }

    private IResourceCalendarService calendar(CapacityCoverageStatus coverageStatus) {
        return (tenantId, userIds, planningStart, planningEnd) -> new CalendarCapacityResult(
                List.of(new ResourceCapacitySlot(100L, START, START + EIGHT_HOURS, EIGHT_HOURS)),
                coverageStatus,
                coverageStatus == CapacityCoverageStatus.FULL ? List.of() : List.of(100L),
                START,
                List.of()
        );
    }

    private ResourceWorkloadPlan plan(Long workItemId, Long start, Long end) {
        return new ResourceWorkloadPlan(null, workItemId, start, end);
    }

    private ResourceWorkloadPlan planWithId(Long id, Long workItemId, Long start, Long end) {
        return new ResourceWorkloadPlan(id, workItemId, start, end);
    }

    private ResourceWorkloadItem item(Long id, Long projectId, Long assigneeId) {
        return new ResourceWorkloadItem(id, projectId, assigneeId, null, null);
    }

    private ResourceWorkloadItem itemWithEstimate(Long id, Long projectId, Long assigneeId, Long remainingEstimate) {
        return new ResourceWorkloadItem(id, projectId, assigneeId, null, remainingEstimate);
    }
}
