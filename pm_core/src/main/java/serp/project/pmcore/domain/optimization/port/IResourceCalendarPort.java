/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;

import java.util.List;

public interface IResourceCalendarPort {
    CalendarCapacityResult resolveWorkingCapacity(Long tenantId,
                                                  List<Long> userIds,
                                                  Long planningStart,
                                                  Long planningEnd);
}
