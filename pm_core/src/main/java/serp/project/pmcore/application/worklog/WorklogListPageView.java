/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.worklog;

import java.util.List;

public record WorklogListPageView(
        List<WorklogView> items,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize,
        Long workItemId,
        Long workItemTimeSpent,
        Long workItemTimeRemainingEstimate
) {
}
