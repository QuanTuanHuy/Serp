/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.resolution.command.update;

import serp.project.pmcore.application.resolution.ResolutionView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.workitem.dto.ResolutionUpdateData;

public record UpdateResolutionCommand(
        Long id,
        ResolutionUpdateData data,
        Long tenantId,
        Long userId
) implements ICommand<ResolutionView> {
}
