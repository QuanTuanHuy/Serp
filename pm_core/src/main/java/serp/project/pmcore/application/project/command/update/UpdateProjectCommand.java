/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;

import java.util.Set;

public record UpdateProjectCommand(
        Long projectId,
        ProjectUpdateData data,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<UpdateProjectResult> {
}
