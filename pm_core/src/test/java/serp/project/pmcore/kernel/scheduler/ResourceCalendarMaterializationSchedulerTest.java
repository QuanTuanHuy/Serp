/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.application.resourcecalendar.command.assignment.ReplaceResourceCalendarAssignmentsCommandHandler;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ResourceCalendarMaterializationSchedulerTest {
    @Mock
    private IResourceCalendarAssignmentPort assignmentPort;

    @Mock
    private ReplaceResourceCalendarAssignmentsCommandHandler assignmentHandler;

    @InjectMocks
    private ResourceCalendarMaterializationScheduler scheduler;

    @Test
    void refreshShouldNoopUntilTenantEnumerationIsAvailable() {
        scheduler.refreshMaterializedSlots();

        verifyNoInteractions(assignmentPort, assignmentHandler);
    }
}
