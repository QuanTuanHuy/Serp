/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.command;

import serp.project.pmcore.application.project.permission.query.ProjectPermissionSettingsView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.List;
import java.util.Set;

public record ReplaceProjectPermissionGrantsCommand(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        List<PermissionGrantData> grants
) implements ICommand<ProjectPermissionSettingsView> {
    public ReplaceProjectPermissionGrantsCommand {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
        grants = grants == null ? List.of() : List.copyOf(grants);
    }
}
