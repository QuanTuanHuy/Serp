/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.entity.ActivityEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.port.store.IActivityPort;
import serp.project.crm.core.port.store.IContactPort;
import serp.project.crm.core.port.store.ILeadPort;
import serp.project.crm.core.port.store.IOpportunityPort;
import serp.project.crm.core.port.store.ITeamMemberPort;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private IActivityPort activityPort;

    @Mock
    private ILeadPort leadPort;

    @Mock
    private IOpportunityPort opportunityPort;

    @Mock
    private IAccountPort accountPort;

    @Mock
    private ITeamMemberPort teamMemberPort;

    @Mock
    private IContactPort contactPort;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void getUpcomingActivitiesShouldQueryByRequestedRangeInEpochMillis() {
        LocalDateTime startDate = LocalDateTime.of(2026, 5, 1, 8, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 5, 10, 18, 30);
        Long tenantId = 99L;
        Long expectedStart = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long expectedEnd = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        List<ActivityEntity> expectedActivities = List.of(ActivityEntity.builder().id(1L).build());

        when(activityPort.findUpcomingActivities(tenantId, expectedStart, expectedEnd)).thenReturn(expectedActivities);

        List<ActivityEntity> result = activityService.getUpcomingActivities(startDate, endDate, tenantId);

        assertSame(expectedActivities, result);
        verify(activityPort).findUpcomingActivities(tenantId, expectedStart, expectedEnd);
        verifyNoMoreInteractions(activityPort);
    }

    @Test
    void getUpcomingActivitiesShouldRejectInvalidDateRange() {
        LocalDateTime startDate = LocalDateTime.of(2026, 5, 10, 18, 30);
        LocalDateTime endDate = LocalDateTime.of(2026, 5, 1, 8, 0);

        AppException exception = assertThrows(AppException.class,
                () -> activityService.getUpcomingActivities(startDate, endDate, 99L));

        assertEquals(ErrorMessage.INVALID_DATE_RANGE, exception.getMessage());
    }
}
