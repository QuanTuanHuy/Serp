/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.calendar;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleCalendarCriteria;

import java.util.Set;

public record ListWorkItemScheduleCalendarQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        WorkItemScheduleCalendarCriteria criteria
) implements IQuery<WorkItemScheduleCalendarPageView> {
}
