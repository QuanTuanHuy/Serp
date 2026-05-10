/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.timeline;

import java.util.List;

public record WorkItemTimelinePageView(
        List<WorkItemTimelineItemView> items,
        List<WorkItemTimelineDependencyView> dependencies,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize
) {
}
