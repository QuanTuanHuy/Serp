/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelink.query.list;

import serp.project.pmcore.application.issuelink.IssueLinkListItemView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ListIssueLinksQuery(
        Long projectId,
        Long workItemId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<List<IssueLinkListItemView>> {
    public ListIssueLinksQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
