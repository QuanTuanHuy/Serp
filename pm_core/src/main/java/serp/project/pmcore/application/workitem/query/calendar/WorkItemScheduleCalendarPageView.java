/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.calendar;

import java.util.List;

public record WorkItemScheduleCalendarPageView(
        List<WorkItemScheduleAllocationCalendarItemView> items,
        long totalItems,
        int totalPages,
        int currentPage,
        int pageSize
) {
}
