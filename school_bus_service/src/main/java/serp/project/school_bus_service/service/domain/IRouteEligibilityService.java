package serp.project.school_bus_service.service.domain;

import serp.project.school_bus_service.dto.response.EligibleStudentResponse;
import serp.project.school_bus_service.dto.response.PlanningPreviewResponse;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * Domain policy service — determines which subscriptions are eligible for route planning.
 * Eligibility rules:
 *  - OUTBOUND: tripOption MORNING or ROUND_TRIP, subscription ACTIVE,
 *              effective range contains serviceDate, dayOfWeek selected, not in active pause period,
 *              has pickupPoint linked to school with PICKUP_TO_SCHOOL window for the schedule.
 *  - RETURN:   tripOption AFTERNOON or ROUND_TRIP, similar rules with dropoffPoint + DROPOFF_FROM_SCHOOL window.
 */
public interface IRouteEligibilityService {

    /**
     * Load all StudentSubscriptionEntity records eligible for the given context.
     */
    List<StudentSubscriptionEntity> findEligible(
            Long schoolId,
            Long schoolScheduleId,
            String routeDirection,
            LocalDate serviceDate,
            Long tenantId);

    /**
     * Build the full preview response: eligible students, pickup/dropoff point summaries, and issues.
     */
    PlanningPreviewResponse buildPreview(
            serp.project.school_bus_service.dto.request.PlanningSessionPreviewRequest request,
            Long tenantId);

    /**
     * Map a single subscription to its EligibleStudentResponse for the given direction.
     */
    EligibleStudentResponse toEligibleStudentResponse(
            StudentSubscriptionEntity subscription,
            Long schoolScheduleId,
            String routeDirection,
            Long tenantId);
}
