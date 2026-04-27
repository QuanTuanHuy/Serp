/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.worklog.WorklogDetailView;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record GetWorklogByIdQuery(
        Long projectId,
        Long workItemId,
        Long worklogId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<WorklogDetailView> {

    public GetWorklogByIdQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
