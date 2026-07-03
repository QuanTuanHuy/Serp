/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.CalendarCapacityResult;

import java.util.List;

/**
 * Service interface for resolving working capacity calendars for resources.
 * This service determines the available time intervals (capacity slots) for users
 * based on their working calendar settings.
 */
public interface IResourceCalendarService {

    /**
     * Resolves the working capacity slots (available time) for a list of users within a planning window.
     * If user-specific working calendars are not found, the service falls back to default settings
     * (e.g., weekdays 8-hour workday, UTC+7) and registers warnings.
     *
     * @param tenantId      the unique identifier of the tenant owning the calendar data
     * @param userIds       the list of user identifiers whose capacities are resolved
     * @param planningStart the start timestamp (in milliseconds) of the planning window
     * @param planningEnd   the end timestamp (in milliseconds) of the planning window
     * @return the resolved {@link CalendarCapacityResult} containing available slots, fallback details, and warnings
     */
    CalendarCapacityResult resolveWorkingCapacity(Long tenantId,
                                                  List<Long> userIds,
                                                  Long planningStart,
                                                  Long planningEnd);
}
