/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.unarchive;

import serp.project.pmcore.application.project.command.update.UpdateProjectResult;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Set;

public record UnarchiveProjectCommand(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<UpdateProjectResult> {
}
