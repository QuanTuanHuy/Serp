/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.priorityscheme.command.manageitems;

import serp.project.pmcore.application.priorityscheme.PrioritySchemeDetailView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.List;

public record ManagePrioritySchemeItemsCommand(
        Long schemeId,
        List<Long> priorityIds,
        Long tenantId,
        Long userId
) implements ICommand<PrioritySchemeDetailView> {
}
