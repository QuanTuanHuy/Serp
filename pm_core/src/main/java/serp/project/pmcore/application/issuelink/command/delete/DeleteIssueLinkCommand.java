/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.delete;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record DeleteIssueLinkCommand(
        Long projectId,
        Long workItemId,
        Long linkId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<DeleteIssueLinkResult> {
    public DeleteIssueLinkCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
