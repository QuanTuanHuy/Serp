/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;

import java.util.Set;

public record ListWorkItemTimelineQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemTimelineCriteria criteria,
        boolean includeDependencies
) implements IQuery<WorkItemTimelinePageView> {
}
