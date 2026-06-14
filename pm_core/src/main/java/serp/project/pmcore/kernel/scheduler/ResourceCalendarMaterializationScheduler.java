/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import serp.project.pmcore.application.resourcecalendar.command.assignment.ReplaceResourceCalendarAssignmentsCommandHandler;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;

@Component
@RequiredArgsConstructor
public class ResourceCalendarMaterializationScheduler {
    private final IResourceCalendarAssignmentPort assignmentPort;
    private final ReplaceResourceCalendarAssignmentsCommandHandler assignmentHandler;

    @Scheduled(cron = "0 15 2 * * *")
    public void refreshMaterializedSlots() {
        // Tenant enumeration is not available in pm_core yet; command-triggered materialization
        // is the active MVP path, and this component is ready for wiring when tenant listing exists.
    }
}
