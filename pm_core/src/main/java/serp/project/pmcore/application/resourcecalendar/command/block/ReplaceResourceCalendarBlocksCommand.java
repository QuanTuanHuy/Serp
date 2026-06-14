/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resourcecalendar.command.block;

import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record ReplaceResourceCalendarBlocksCommand(
        Long tenantId,
        Long profileId,
        List<Block> blocks
) implements ICommand<List<ResourceCalendarSettingsOverviewView.BlockView>> {
    public record Block(
            Integer dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            BigDecimal capacityFactor
    ) {
    }
}
