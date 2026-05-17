/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.create;

import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record CreateResolutionCommand(
        String name,
        String description,
        Integer sequence,
        Long tenantId,
        Long userId
) implements ICommand<ResolutionView> {
}
