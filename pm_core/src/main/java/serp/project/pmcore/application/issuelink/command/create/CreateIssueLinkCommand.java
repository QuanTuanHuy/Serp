/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.command.create;

import serp.project.pmcore.application.issuelink.IssueLinkView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record CreateIssueLinkCommand(
        Long projectId,
        Long workItemId,
        Long targetId,
        Long linkTypeId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements ICommand<IssueLinkView> {
    public CreateIssueLinkCommand {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
