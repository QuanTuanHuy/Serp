/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.assignment;

import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.time.LocalDate;
import java.util.List;

public record ReplaceResourceCalendarAssignmentsCommand(
        Long tenantId,
        Long profileId,
        List<Assignment> assignments
) implements ICommand<List<ResourceCalendarSettingsOverviewView.AssignmentView>> {
    public record Assignment(
            Long userId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
    }
}
