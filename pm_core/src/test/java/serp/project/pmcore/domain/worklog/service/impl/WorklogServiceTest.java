/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemWritePort;
import serp.project.pmcore.domain.worklog.port.IWorklogPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorklogServiceTest {

    @Mock
    private IWorklogPort worklogPort;

    @Mock
    private IWorkItemWritePort workItemWritePort;

    private WorklogService worklogService;

    @BeforeEach
    void setUp() {
        worklogService = new WorklogService(worklogPort, workItemWritePort);
    }

    @Test
    void refreshWorkItemTimeTrackingShouldClampRemainingEstimateAtZero() {
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(10L)
                .tenantId(1L)
                .timeOriginalEstimate(3_600L)
                .timeRemainingEstimate(3_600L)
                .timeSpent(0L)
                .build();
        when(worklogPort.sumActiveTimeSpentByWorkItemId(10L, 1L)).thenReturn(4_200L);
        when(workItemWritePort.saveWorkItem(any(WorkItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkItemEntity refreshed = worklogService.refreshWorkItemTimeTracking(workItem, 99L);

        assertEquals(4_200L, refreshed.getTimeSpent());
        assertEquals(0L, refreshed.getTimeRemainingEstimate());
        assertEquals(99L, refreshed.getUpdatedBy());
    }

    @Test
    void refreshWorkItemTimeTrackingShouldKeepRemainingNullWhenOriginalEstimateMissing() {
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(11L)
                .tenantId(1L)
                .timeOriginalEstimate(null)
                .timeRemainingEstimate(1_000L)
                .timeSpent(500L)
                .build();
        when(worklogPort.sumActiveTimeSpentByWorkItemId(11L, 1L)).thenReturn(1_200L);
        when(workItemWritePort.saveWorkItem(any(WorkItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkItemEntity refreshed = worklogService.refreshWorkItemTimeTracking(workItem, 99L);

        assertEquals(1_200L, refreshed.getTimeSpent());
        assertNull(refreshed.getTimeRemainingEstimate());
    }
}
